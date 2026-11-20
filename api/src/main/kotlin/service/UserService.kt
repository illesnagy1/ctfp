package com.ctfp.service

import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.AdminUserResponse
import com.ctfp.dto.UserResponse
import com.ctfp.dto.UserUpdateRequest
import com.ctfp.dto.applyProfile
import com.ctfp.dto.toAdminDto
import com.ctfp.dto.toDto
import com.ctfp.dto.toProfileUpdate
import com.ctfp.service.helper.PasswordHasher
import com.ctfp.service.helper.activeAdmin
import com.ctfp.service.helper.audit
import com.ctfp.service.helper.deleteAccount
import com.ctfp.service.helper.ensureAvailable
import com.ctfp.service.helper.findUser
import com.ctfp.service.helper.lockUser
import com.ctfp.service.helper.patched
import com.ctfp.service.helper.validated
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.slf4j.event.Level
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

class UserService(
    private val hasher: PasswordHasher,
) {
    suspend fun getUser(id: Int): UserResponse =
        withTransaction {
            findUser(id).takeUnless { it.isHidden || it.isBanned }?.toDto()
                ?: throw ApiException(HttpStatusCode.NotFound, "User $id not found")
        }

    /** The caller's own account, with the fields only they and admins see. */
    suspend fun getMe(id: Int): AdminUserResponse =
        withTransaction {
            findUser(id).toAdminDto()
        }

    suspend fun getAllUsers(): List<UserResponse> =
        withTransaction {
            UserDAO.find { (UserTable.isHidden eq false) and (UserTable.isBanned eq false) }.map { it.toDto() }
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

    /** Deletes [id]'s own account after checking [password]; the last admin can't, or no one could manage the CTF. */
    suspend fun deleteMe(
        id: Int,
        password: String,
    ) {
        val hash = withTransaction { findUser(id).passwordHash }
        if (!hasher.verify(password, hash)) {
            audit("user_delete_fail", "user" to id, "reason" to "wrong password", level = Level.WARN)
            throw ApiException(HttpStatusCode.Forbidden, "Wrong password")
        }
        withTransaction {
            val user = lockUser(id)
            if (user.role == UserRole.ADMIN && UserDAO.find { activeAdmin }.forUpdate().count() <= 1) {
                throw ApiException(HttpStatusCode.Conflict, "You are the last admin; make someone else admin first")
            }
            deleteAccount(user)
        }
        audit("user_deleted", "user" to id, "by" to "self")
    }

    private fun saveProfile(
        dao: UserDAO,
        user: UserUpdateRequest,
    ) {
        ensureAvailable(user.username, user.email, exceptId = dao.id.value)
        dao.applyProfile(user)
    }
}
