package com.ctfp.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable


@Serializable
data class TeamMember(
    val teamId: Int,
    val userId: Int,
    val joinedAt: Instant,
)
