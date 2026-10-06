package com.ctfp.service

import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.AdminUserCreateRequest
import com.ctfp.dto.AdminUserResponse
import com.ctfp.dto.AdminUserUpdateRequest
import com.ctfp.dto.UserProfileFields
import com.ctfp.dto.UserResponse
import com.ctfp.dto.UserUpdateRequest
import com.ctfp.dto.applyAdmin
import com.ctfp.dto.applyProfile
import com.ctfp.dto.toAdminDto
import com.ctfp.dto.toDto
import com.ctfp.service.helper.PasswordHasher
import com.ctfp.service.helper.validateEmail
import com.ctfp.service.helper.validatePassword
import com.ctfp.service.helper.validateUsername
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere


class UserService(
    private val hasher: PasswordHasher,
) {
    /** Throws 403 unless [userId] is an admin that isn't banned. Checked against the DB, not the JWT role claim, so demotions apply immediately. */
    suspend fun requireAdmin(userId: Int) {
        if (!isAdmin(userId)) throw ApiException(HttpStatusCode.Forbidden, "Admin access required")
    }

    suspend fun isAdmin(userId: Int): Boolean = withTransaction {
        UserDAO.findById(userId)?.let { it.role == UserRole.ADMIN && !it.isBanned } ?: false
    }

    /** Public view; hidden and banned users are reported as not found. */
    suspend fun getUser(id: Int): UserResponse = withTransaction {
        findUser(id).takeUnless { it.isHidden || it.isBanned }?.toDto()
            ?: throw ApiException(HttpStatusCode.NotFound, "User $id not found")
    }

    suspend fun getAdminUser(id: Int): AdminUserResponse = withTransaction {
        findUser(id).toAdminDto()
    }

    /** Public listing; excludes hidden and banned users. */
    suspend fun getAllUsers(): List<UserResponse> = withTransaction {
        UserDAO.find { (UserTable.isHidden eq false) and (UserTable.isBanned eq false) }.map { it.toDto() }
    }

    suspend fun getAllAdminUsers(): List<AdminUserResponse> = withTransaction {
        UserDAO.all().map { it.toAdminDto() }
    }

    suspend fun createUser(user: AdminUserCreateRequest): Int {
        validate(user)
        validatePassword(user.password)
        val passwordHash = hasher.hash(user.password)

        return withTransaction {
            ensureAvailable(user)
            UserDAO.new {
                applyAdmin(user)
                this.passwordHash = passwordHash
            }.id.value
        }
    }

    suspend fun updateUser(id: Int, user: UserUpdateRequest) {
        validate(user)
        withTransaction {
            ensureAvailable(user, exceptId = id)
            findUser(id).applyProfile(user)
        }
    }

    suspend fun updateUser(id: Int, user: AdminUserUpdateRequest) {
        validate(user)
        withTransaction {
            ensureAvailable(user, exceptId = id)
            findUser(id).applyAdmin(user)
        }
    }

    suspend fun updateUsers(users: Map<Int, AdminUserUpdateRequest>) {
        users.values.forEach(::validate)
        withTransaction {
            users.forEach { (id, user) ->
                ensureAvailable(user, exceptId = id)
                findUser(id).applyAdmin(user)
            }
        }
    }

    suspend fun deleteUser(id: Int) = withTransaction {
        findUser(id).delete()
    }

    suspend fun deleteUsers(ids: List<Int>) = withTransaction {
        UserTable.deleteWhere { UserTable.id inList ids }
    }

    /** Must run inside a transaction. */
    private fun findUser(id: Int): UserDAO =
        UserDAO.findById(id) ?: throw ApiException(HttpStatusCode.NotFound, "User $id not found")

    private fun validate(user: UserProfileFields) {
        validateUsername(user.username)
        validateEmail(user.email)
    }

    /** Must run inside a transaction. */
    private fun ensureAvailable(user: UserProfileFields, exceptId: Int? = null) {
        val clash = (UserTable.username eq user.username) or (UserTable.email eq user.email.lowercase())
        val query = if (exceptId == null) clash else clash and (UserTable.id neq exceptId)
        if (!UserDAO.find { query }.empty()) {
            throw ApiException(HttpStatusCode.Conflict, "Username or email is already taken")
        }
    }
}
