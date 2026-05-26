package com.ctfp.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable


@Serializable
data class Flag(
    val challengeId: Int,
    val pattern: String,
    val isCaseSensitive: Boolean,
    val baseValue: Int,
    val decayValue: Int?,
    val minValue: Int? = null,
    val decayFun: String = "static",
    val sortOrder: Int = 1,
    val createdAt: Instant
)
