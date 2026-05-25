package com.ctfp.repository.postgres

import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.RegistrationCodeTable
import com.ctfp.dto.User
import com.ctfp.repository.IUserRepository
import org.jetbrains.exposed.v1.core.dao.id.EntityID


class PostgresUserRepository : IUserRepository {
    override suspend fun getUser(id: Int): User = withTransaction {
        UserDAO[id].toModel()
    }

    override suspend fun getAllUsers(): List<User> = withTransaction {
        UserDAO.all().map { it.toModel() }
    }

    override suspend fun createUser(user: User) = withTransaction {
        UserDAO.new {
            codeId = EntityID(user.codeId, RegistrationCodeTable)
            username = user.username
            email = user.email
            passwordHash = user.passwordHash
            language = user.language
            country = user.country
            website = user.website
            role = user.role
            isVerified = user.isVerified
            isHidden = user.isHidden
            isBanned = user.isBanned
            totpSecret = user.totpSecret
            totpEnabled = user.totpEnabled
            soundEnabled = user.soundEnabled
            visibilitySettings = user.visibilitySettings
            createdAt = user.createdAt
            updatedAt = user.updatedAt
        }.id.value
    }

    override suspend fun updateUser(id: Int, user: User) = withTransaction {
        val dbUser = UserDAO[id]
        dbUser.run {
            codeId = EntityID(user.codeId, RegistrationCodeTable)
            username = user.username
            email = user.email
            passwordHash = user.passwordHash
            language = user.language
            country = user.country
            website = user.website
            role = user.role
            isVerified = user.isVerified
            isHidden = user.isHidden
            isBanned = user.isBanned
            totpSecret = user.totpSecret
            totpEnabled = user.totpEnabled
            soundEnabled = user.soundEnabled
            visibilitySettings = user.visibilitySettings
            createdAt = user.createdAt
            updatedAt = user.updatedAt
        }
    }

    override suspend fun deleteUser(id: Int) = withTransaction {
        UserDAO[id].delete()
    }
}
