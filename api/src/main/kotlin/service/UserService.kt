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
import com.ctfp.service.helper.validatePassword
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import com.ctfp.service.helper.ensureAvailable
import com.ctfp.service.helper.findUser
import com.ctfp.service.helper.validate


class UserService(
    private val hasher: PasswordHasher,
) {
    suspend fun getUser(id: Int): UserResponse = withTransaction {
        findUser(id).takeUnless { it.isHidden || it.isBanned }?.toDto()
            ?: throw ApiException(HttpStatusCode.NotFound, "User $id not found")
    }

    suspend fun getAdminUser(id: Int): AdminUserResponse = withTransaction {
        findUser(id).toAdminDto()
    }

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
            ensureAvailable(user.username, user.email)
            UserDAO.new {
                applyAdmin(user)
                this.passwordHash = passwordHash
            }.id.value
        }
    }

    suspend fun updateUser(id: Int, user: UserUpdateRequest) {
        validate(user)
        withTransaction {
            ensureAvailable(user.username, user.email, exceptId = id)
            findUser(id).applyProfile(user)
        }
    }

    suspend fun updateUser(id: Int, user: AdminUserUpdateRequest) {
        validate(user)
        withTransaction {
            ensureAvailable(user.username, user.email, exceptId = id)
            findUser(id).applyAdmin(user)
        }
    }

    suspend fun updateUsers(users: Map<Int, AdminUserUpdateRequest>) {
        users.values.forEach(::validate)
        withTransaction {
            users.forEach { (id, user) ->
                ensureAvailable(user.username, user.email, exceptId = id)
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
}
