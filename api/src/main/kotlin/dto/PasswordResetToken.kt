package com.ctfp.dto

import com.ctfp.domain.dao.PasswordResetTokenDAO
import com.ctfp.domain.dao.UserDAO
import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class PasswordResetTokenRequest(
    val userId: Int,
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    val token: String,
    val expiresAt: Instant,
)

@Serializable
data class PasswordResetTokenResponse(
    val id: Int,
    val userId: Int,
    val token: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val usedAt: Instant?,
)

fun PasswordResetTokenDAO.apply(dto: PasswordResetTokenRequest) {
    user = UserDAO[dto.userId]
    token = dto.token
    expiresAt = dto.expiresAt
}

fun PasswordResetTokenDAO.toDto() =
    PasswordResetTokenResponse(
        id = id.value,
        userId = user.id.value,
        token = token,
        createdAt = createdAt,
        expiresAt = expiresAt,
        usedAt = usedAt,
    )
