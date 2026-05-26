package com.ctfp.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable


@Serializable
data class PasswordResetToken(
    val userId: Int,
    val token: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val usedAt: Instant?,
)
