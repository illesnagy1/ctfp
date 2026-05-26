package com.ctfp.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable


@Serializable
data class HintUnlock(
    val hintId: Int,
    val userId: Int,
    val teamId: Int?,
    val unlockedAt: Instant
)
