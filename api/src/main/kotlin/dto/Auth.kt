package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.UserDAO
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @JsonSchema.Pattern(Limits.USERNAME_PATTERN)
    @JsonSchema.MinLength(Limits.USERNAME_MIN)
    @JsonSchema.MaxLength(Limits.USERNAME_MAX)
    val username: String,
    @JsonSchema.Format("email")
    @JsonSchema.MaxLength(Limits.EMAIL_MAX)
    val email: String,
    @JsonSchema.MinLength(Limits.PASSWORD_MIN)
    @JsonSchema.MaxLength(Limits.PASSWORD_MAX)
    val password: String,
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    val registrationCode: String? = null
)

@Serializable
data class LoginRequest(
    @JsonSchema.Description("Username or email.")
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.EMAIL_MAX)
    val login: String,
    @JsonSchema.MinLength(1)
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
