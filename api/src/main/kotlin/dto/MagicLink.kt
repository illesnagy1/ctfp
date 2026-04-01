package com.ctfp.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class MagicLink(
    val userId: Int,
    val token: String,
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val usedAt: LocalDateTime?
)
