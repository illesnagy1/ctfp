package com.ctfp.service

import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.AdminUserCreateRequest
import com.ctfp.dto.AdminUserResponse
import com.ctfp.dto.AdminUserUpdateRequest
import com.ctfp.dto.UserResponse
import com.ctfp.dto.UserUpdateRequest
import com.ctfp.dto.applyAdmin
import com.ctfp.dto.applyProfile
import com.ctfp.dto.etag
import com.ctfp.dto.toAdminDto
import com.ctfp.dto.toDto
import com.ctfp.dto.toProfileUpdate
import com.ctfp.dto.toUpdateRequest
import com.ctfp.service.helper.PasswordHasher
import com.ctfp.service.helper.ensureAvailable
import com.ctfp.service.helper.findUser
import com.ctfp.service.helper.patched
import com.ctfp.service.helper.validated
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere

class UserService(
    private val hasher: PasswordHasher,
) {
    suspend fun getUser(id: Int): UserResponse =
        withTransaction {
            findUser(id).takeUnless { it.isHidden || it.isBanned }?.toDto()
                ?: throw ApiException(HttpStatusCode.NotFound, "User $id not found")
        }

    suspend fun getAdminUser(id: Int): AdminUserResponse =
        withTransaction {
            findUser(id).toAdminDto()
        }

    suspend fun getAllUsers(): List<UserResponse> =
        withTransaction {
            UserDAO.find { (UserTable.isHidden eq false) and (UserTable.isBanned eq false) }.map { it.toDto() }
        }

    suspend fun getAllAdminUsers(): List<AdminUserResponse> =
        withTransaction {
            UserDAO.all().map { it.toAdminDto() }
        }

    suspend fun createUser(user: AdminUserCreateRequest): Int {
        val passwordHash = hasher.hash(user.password)

        return withTransaction {
            ensureAvailable(user.username, user.email)
            UserDAO
                .new {
                    applyAdmin(user)
                    this.passwordHash = passwordHash
                }.id.value
        }
    }

    /** Replaces [id]'s own profile; [ifMatch] as in [lockUser]. */
    suspend fun updateUser(
        id: Int,
        user: UserUpdateRequest,
        ifMatch: String? = null,
    ) = withTransaction {
        val dao = lockUser(id, ifMatch)
        saveProfile(dao, user)
    }

    /** Changes the fields of [id]'s own profile that [patch] names (JSON Merge Patch). */
    suspend fun patchUser(
        id: Int,
        patch: JsonObject,
        ifMatch: String? = null,
    ) = withTransaction {
        val dao = lockUser(id, ifMatch)
        saveProfile(dao, validated(patched(serializer(), dao.toAdminDto().toProfileUpdate(), patch)))
    }

    /** Replaces [id]'s account; [actorId] is the admin, who can't ban or demote themselves. */
    suspend fun updateUser(
        id: Int,
        user: AdminUserUpdateRequest,
        actorId: Int,
        ifMatch: String? = null,
    ) = withTransaction {
        lockAdmins(actorId)
        val dao = lockUser(id, ifMatch)
        saveAdmin(dao, user, actorId)
    }

    /** Changes the fields of [id]'s account that [patch] names (JSON Merge Patch). */
    suspend fun patchUser(
        id: Int,
        patch: JsonObject,
        actorId: Int,
        ifMatch: String? = null,
    ) = withTransaction {
        lockAdmins(actorId)
        val dao = lockUser(id, ifMatch)
        saveAdmin(dao, validated(patched(serializer(), dao.toAdminDto().toUpdateRequest(), patch)), actorId)
    }

    /** Replaces several accounts at once, all or nothing; [actorId] as in [updateUser]. */
    suspend fun updateUsers(
        users: Map<Int, AdminUserUpdateRequest>,
        actorId: Int,
    ) {
        withTransaction {
            lockAdmins(actorId)
            // In id order, so two bulk writes lock rows in the same order and can't deadlock.
            for ((id, user) in users.toSortedMap()) saveAdmin(lockUser(id), user, actorId)
        }
    }

    /** Patches several accounts at once (`{"<id>": patch}`), all or nothing; field errors point at `/<id>/…`. */
    suspend fun patchUsers(
        patches: Map<Int, JsonObject>,
        actorId: Int,
    ) {
        withTransaction {
            lockAdmins(actorId)
            // In id order, see updateUsers.
            val users = patches.keys.sorted().associateWith { lockUser(it) }
            val updated =
                validated(
                    patches.mapValues { (id, patch) ->
                        patched(serializer(), users.getValue(id).toAdminDto().toUpdateRequest(), patch)
                    },
                )
            for ((id, user) in updated) saveAdmin(users.getValue(id), user, actorId)
        }
    }

    private fun saveProfile(
        dao: UserDAO,
        user: UserUpdateRequest,
    ) {
        ensureAvailable(user.username, user.email, exceptId = dao.id.value)
        dao.applyProfile(user)
    }

    private fun saveAdmin(
        dao: UserDAO,
        user: AdminUserUpdateRequest,
        actorId: Int,
    ) {
        ensureNotSelfLockout(dao.id.value, user, actorId)
        ensureAvailable(user.username, user.email, exceptId = dao.id.value)
        dao.applyAdmin(user)
    }

    /**
     * Banning or demoting themselves would lock the admin out (banned users lose admin rights too)
     */
    private fun ensureNotSelfLockout(
        id: Int,
        user: AdminUserUpdateRequest,
        actorId: Int,
    ) {
        if (id != actorId) return
        if (user.isBanned) throw ApiException(HttpStatusCode.BadRequest, "You cannot ban yourself")
        if (user.role != UserRole.ADMIN) throw ApiException(HttpStatusCode.BadRequest, "You cannot change your own role")
    }

    /** [actorId] is the admin deleting. */
    suspend fun deleteUser(
        id: Int,
        actorId: Int,
    ) = withTransaction {
        lockAdmins(actorId)
        findUser(id).delete()
    }

    /** [actorId] is the admin deleting. */
    suspend fun deleteUsers(
        ids: List<Int>,
        actorId: Int,
    ) = withTransaction {
        lockAdmins(actorId)
        UserTable.deleteWhere { UserTable.id inList ids }
    }
}

/**
 * [id]'s row, locked until the transaction ends, so writes to one account run one after another: a patch then applies
 * to the latest state, not one another write is replacing. With [ifMatch] (an If-Match header), 412 unless it names
 * the current version: the client edited what it saw, and someone has changed it since (RFC 9110 §13.1.1).
 */
private fun lockUser(
    id: Int,
    ifMatch: String? = null,
): UserDAO {
    val user =
        UserDAO.find { UserTable.id eq id }.forUpdate().singleOrNull()
            ?: throw ApiException(HttpStatusCode.NotFound, "User $id not found")
    val current = etag(user.version)
    if (ifMatch != null && ifMatch.split(',').map(String::trim).none { it == "*" || it == current }) {
        throw ApiException(HttpStatusCode.PreconditionFailed, "This user was changed in the meantime; reload and try again")
    }
    return user
}

/**
 * Locks the admin rows until the transaction ends and checks [actorId] is still one of them; call first in a
 * transaction that changes users. The admin check ran when the request started, so without this two admins
 * demoting or deleting each other at the same time could both succeed and leave no admin.
 */
private fun lockAdmins(actorId: Int) {
    val admins = UserDAO.find { (UserTable.role eq UserRole.ADMIN) and (UserTable.isBanned eq false) }.forUpdate()
    if (admins.none { it.id.value == actorId }) throw ApiException(HttpStatusCode.Forbidden, "Admin access required")
}
