package com.ctfp.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable


@Serializable
data class Submission(
    val userId: Int,
    val teamId: Int?,
    val challengeId: Int,
    val flagId: Int?,
    val submittedAt: Instant,
)
