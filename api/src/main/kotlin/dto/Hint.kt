package com.ctfp.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable


@Serializable
data class Hint(
    val challengeId: Int,
    val body: String,
    val cost: Int = 0,
    val createdAt: Instant
)
