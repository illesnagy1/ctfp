package com.ctfp.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable


@Serializable
data class RegistrationCode(
    val code: String,
    val maxUses: Int,
    val createdAt: Instant,
    val expiresAt: Instant,
)
