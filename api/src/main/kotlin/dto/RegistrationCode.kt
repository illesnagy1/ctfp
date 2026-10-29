package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import com.ctfp.domain.dao.RegistrationCodeDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationCodeRequest(
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    val code: String,
    @JsonSchema.Minimum(0.0)
    val maxUses: Int,
    val expiresAt: Instant
)

@Serializable
data class RegistrationCodeResponse(
    val id: Int,
    val code: String,
    val maxUses: Int,
    val createdAt: Instant,
    val expiresAt: Instant
)

fun RegistrationCodeDAO.apply(dto: RegistrationCodeRequest) {
    code = dto.code
    maxUses = dto.maxUses
    expiresAt = dto.expiresAt
}

fun RegistrationCodeDAO.toDto() = RegistrationCodeResponse(
    id = id.value,
    code = code,
    maxUses = maxUses,
    createdAt = createdAt,
    expiresAt = expiresAt
)
