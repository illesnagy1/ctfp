package com.ctfp.repository

import com.ctfp.dto.User


interface UserRepository {
    suspend fun getUser(id: Int): User
    suspend fun getAllUsers(): List<User>
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun deleteUser(id: Int)
}
