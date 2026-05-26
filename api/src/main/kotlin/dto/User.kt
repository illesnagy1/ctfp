package com.ctfp.dto

import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.dao.RegistrationCodeDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(
    val codeId: Int,
    val username: String,
    val email: String,
    val passwordHash: String,
    val language: String?,
    val country: String?,
    val website: String?,
    val role: UserRole,
    val isVerified: Boolean = false,
    val isHidden: Boolean = false,
    val isBanned: Boolean = false,
    val totpSecret: String?,
    val totpEnabled: Boolean = false,
    val soundEnabled: Boolean = true,
    val visibilitySettings: String?
)

@Serializable
data class UserResponse(
    val id: Int,
    val codeId: Int,
    val username: String,
    val email: String,
    val passwordHash: String,
    val language: String?,
    val country: String?,
    val website: String?,
    val role: UserRole,
    val isVerified: Boolean = false,
    val isHidden: Boolean = false,
    val isBanned: Boolean = false,
    val totpSecret: String?,
    val totpEnabled: Boolean = false,
    val soundEnabled: Boolean = true,
    val visibilitySettings: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

fun UserDAO.apply(dto: UserRequest) {
    registrationCode = RegistrationCodeDAO[dto.codeId]
    username = dto.username
    email = dto.email
    passwordHash = dto.passwordHash
    language = dto.language
    country = dto.country
    website = dto.website
    role = dto.role
    isVerified = dto.isVerified
    isHidden = dto.isHidden
    isBanned = dto.isBanned
    totpSecret = dto.totpSecret
    totpEnabled = dto.totpEnabled
    soundEnabled = dto.soundEnabled
    visibilitySettings = dto.visibilitySettings
}

fun UserDAO.toDto() = UserResponse(
    id = id.value,
    codeId = registrationCode.id.value,
    username = username,
    email = email,
    passwordHash = passwordHash,
    language = language,
    country = country,
    website = website,
    role = role,
    isVerified = isVerified,
    isHidden = isHidden,
    isBanned = isBanned,
    totpSecret = totpSecret,
    totpEnabled = totpEnabled,
    soundEnabled = soundEnabled,
    visibilitySettings = visibilitySettings,
    createdAt = createdAt,
    updatedAt = updatedAt
)
