package com.ctfp.service

import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.AdminUserCreateRequest
import com.ctfp.dto.AdminUserResponse
import com.ctfp.dto.AdminUserUpdateRequest
import com.ctfp.dto.applyAdmin
import com.ctfp.dto.toAdminDto
import com.ctfp.dto.toUpdateRequest
import com.ctfp.service.helper.PasswordHasher
import com.ctfp.service.helper.activeAdmin
import com.ctfp.service.helper.deleteAccount
import com.ctfp.service.helper.ensureAvailable
import com.ctfp.service.helper.findUser
import com.ctfp.service.helper.lockUser
import com.ctfp.service.helper.patched
import com.ctfp.service.helper.validated
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

/** Account management for admins; `actorId` is the admin acting. */
class AdminUserService(
    private val hasher: PasswordHasher,
) {
    suspend fun getAdminUser(id: Int): AdminUserResponse =
        withTransaction {
            findUser(id).toAdminDto()
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

    private fun saveAdmin(
        dao: UserDAO,
        user: AdminUserUpdateRequest,
        actorId: Int,
    ) {
        ensureNotSelfLockout(dao.id.value, user, actorId)
        ensureAvailable(user.username, user.email, exceptId = dao.id.value)
        dao.applyAdmin(user)
    }

    /** [actorId] is the admin deleting. */
    suspend fun deleteUser(
        id: Int,
        actorId: Int,
    ) = withTransaction {
        lockAdmins(actorId)
        deleteAccount(findUser(id))
    }
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

/**
 * Locks the admin rows until the transaction ends and checks [actorId] is still one of them; call first in a
 * transaction that changes users. The admin check ran when the request started, so without this two admins
 * demoting or deleting each other at the same time could both succeed and leave no admin.
 */
private fun lockAdmins(actorId: Int) {
    val admins = UserDAO.find { activeAdmin }.forUpdate()
    if (admins.none { it.id.value == actorId }) throw ApiException(HttpStatusCode.Forbidden, "Admin access required")
}
