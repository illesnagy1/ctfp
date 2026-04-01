package com.ctfp.dto

import com.ctfp.config.enum.Language
import com.ctfp.config.enum.UserRole
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class User(
    val id: Int,
    val codeId: Int,
    val username: String,
    val email: String,
    val passwordHash: String,
    val language: Language,
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
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
