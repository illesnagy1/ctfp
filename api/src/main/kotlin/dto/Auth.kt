package com.ctfp.dto

import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.UserDAO
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val registrationCode: String? = null
)

@Serializable
data class LoginRequest(
    /** Username or email. */
    val login: String,
    val password: String
)

@Serializable
data class AuthUser(
    val id: Int,
    val username: String,
    val email: String,
    val role: UserRole
)

@Serializable
data class AuthResponse(
    val token: String,
    /** Unix epoch seconds. */
    val expiresAt: Long,
    val user: AuthUser
)

@Serializable
data class ErrorResponse(
    val message: String
)

fun UserDAO.toAuthUser() = AuthUser(
    id = id.value,
    username = username,
    email = email,
    role = role
)
