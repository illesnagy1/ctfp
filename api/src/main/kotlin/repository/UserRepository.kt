package com.ctfp.repository

import com.ctfp.dto.User


interface UserRepository {
    suspend fun getUser(id: Int): User
    suspend fun getAllUsers(): List<User>
    suspend fun createUser(user: User): Int
    suspend fun updateUser(id: Int, user: User)
    suspend fun deleteUser(id: Int)
}
