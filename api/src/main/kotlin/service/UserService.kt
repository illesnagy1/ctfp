package com.ctfp.service

import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.UserRequest
import com.ctfp.dto.UserResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.deleteWhere


class UserService {
    suspend fun getUser(id: Int): UserResponse = withTransaction {
        UserDAO[id].toDto()
    }

    suspend fun getAllUsers(): List<UserResponse> = withTransaction {
        UserDAO.all().map { it.toDto() }
    }

    suspend fun createUser(user: UserRequest): Int = withTransaction {
        val newUser = UserDAO.new {
            apply(user)
        }
        newUser.id.value
    }

    suspend fun updateUser(id: Int, user: UserRequest) = withTransaction {
        UserDAO.findByIdAndUpdate(id) {
            it.apply(user)
        }
    }

    suspend fun updateUsers(users: Map<Int, UserRequest>) = withTransaction {

    }

    suspend fun deleteUser(id: Int) = withTransaction {
        UserDAO[id].delete()
    }

    suspend fun deleteUsers(ids: List<Int>) = withTransaction {
        UserTable.deleteWhere { UserTable.id inList ids }
    }
}