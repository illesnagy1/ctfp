package com.ctfp.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class Submission(
    val userId: Int,
    val teamId: Int?,
    val challengeId: Int,
    val flagId: Int?,
    val submittedAt: LocalDateTime,
)
