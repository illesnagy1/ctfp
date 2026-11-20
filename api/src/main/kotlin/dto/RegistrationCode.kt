package com.ctfp.dto

import com.ctfp.domain.dao.RegistrationCodeDAO
import com.ctfp.service.helper.sha256Hex
import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class RegistrationCodeRequest(
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    val code: String,
    @JsonSchema.Minimum(0.0)
    val maxUses: Int,
    val expiresAt: Instant,
)

@Serializable
data class RegistrationCodeResponse(
    val id: Int,
    val maxUses: Int,
    val createdAt: Instant,
    val expiresAt: Instant,
)

/** Only the code's hash is kept, so it is shown once: to the admin who chose it. */
fun RegistrationCodeDAO.apply(dto: RegistrationCodeRequest) {
    code = sha256Hex(dto.code.trim())
    maxUses = dto.maxUses
    expiresAt = dto.expiresAt
}

fun RegistrationCodeDAO.toDto() =
    RegistrationCodeResponse(
        id = id.value,
        maxUses = maxUses,
        createdAt = createdAt,
        expiresAt = expiresAt,
    )
