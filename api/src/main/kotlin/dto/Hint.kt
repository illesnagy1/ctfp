package com.ctfp.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class Hint(
    val challengeId: Int,
    val body: String,
    val cost: Int = 0,
    val createdAt: LocalDateTime
)
