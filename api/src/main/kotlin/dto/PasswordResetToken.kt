package com.ctfp.dto

import com.ctfp.domain.dao.PasswordResetTokenDAO
import com.ctfp.domain.model.UserTable
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID

@Serializable
data class PasswordResetTokenRequest(
    val userId: Int,
    val token: String,
    val expiresAt: Instant
)

@Serializable
data class PasswordResetTokenResponse(
    val id: Int,
    val userId: Int,
    val token: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val usedAt: Instant?
)

fun PasswordResetTokenDAO.apply(dto: PasswordResetTokenRequest) {
    userId = EntityID(dto.userId, UserTable)
    token = dto.token
    expiresAt = dto.expiresAt
}

fun PasswordResetTokenDAO.toDto() = PasswordResetTokenResponse(
    id = id.value,
    userId = userId.value,
    token = token,
    createdAt = createdAt,
    expiresAt = expiresAt,
    usedAt = usedAt
)
