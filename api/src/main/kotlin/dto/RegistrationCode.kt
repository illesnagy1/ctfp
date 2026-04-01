package com.ctfp.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class RegistrationCode(
    val code: String,
    val maxUses: Int,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime,
)
