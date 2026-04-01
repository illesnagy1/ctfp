package com.ctfp.repository.postgres

import com.ctfp.dto.User
import com.ctfp.repository.UserRepository


class PostgresUserRepository : UserRepository {
    override suspend fun getUser(id: Int): User {
        TODO("Not yet implemented")
    }

    override suspend fun getAllUsers(): List<User> {
        TODO("Not yet implemented")
    }

    override suspend fun createUser(user: User) {
        TODO("Not yet implemented")
    }

    override suspend fun updateUser(user: User) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(id: Int) {
        TODO("Not yet implemented")
    }
}
