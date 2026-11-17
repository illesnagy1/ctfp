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
import com.ctfp.dto.toAdminDto
import com.ctfp.dto.toDto
import com.ctfp.service.helper.PasswordHasher
import com.ctfp.service.helper.ensureAvailable
import com.ctfp.service.helper.findUser
import io.ktor.http.HttpStatusCode
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

    suspend fun updateUser(
        id: Int,
        user: UserUpdateRequest,
    ) {
        withTransaction {
            ensureAvailable(user.username, user.email, exceptId = id)
            findUser(id).applyProfile(user)
        }
    }

    /** [actorId] is the admin making the change; they can't ban or demote themselves. */
    suspend fun updateUser(
        id: Int,
        user: AdminUserUpdateRequest,
        actorId: Int,
    ) {
        ensureNotSelfLockout(id, user, actorId)
        withTransaction {
            lockAdmins(actorId)
            ensureAvailable(user.username, user.email, exceptId = id)
            findUser(id).applyAdmin(user)
        }
    }

    /** [actorId] is the admin making the change; they can't ban or demote themselves. */
    suspend fun updateUsers(
        users: Map<Int, AdminUserUpdateRequest>,
        actorId: Int,
    ) {
        users.forEach { (id, user) -> ensureNotSelfLockout(id, user, actorId) }
        withTransaction {
            lockAdmins(actorId)
            users.forEach { (id, user) -> ensureAvailable(user.username, user.email, exceptId = id) }
            UserDAO.findAll(users.keys).forEach { it.applyAdmin(users.getValue(it.id.value)) }
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
 * Locks the admin rows until the transaction ends and checks [actorId] is still one of them; call first in a
 * transaction that changes users. The admin check ran when the request started, so without this two admins
 * demoting or deleting each other at the same time could both succeed and leave no admin.
 */
private fun lockAdmins(actorId: Int) {
    val admins = UserDAO.find { (UserTable.role eq UserRole.ADMIN) and (UserTable.isBanned eq false) }.forUpdate()
    if (admins.none { it.id.value == actorId }) throw ApiException(HttpStatusCode.Forbidden, "Admin access required")
}
