package com.ctfp.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class HintUnlock(
    val hintId: Int,
    val userId: Int,
    val teamId: Int?,
    val unlockedAt: LocalDateTime
)
