package com.ctfp.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class TeamMember(
    val teamId: Int,
    val userId: Int,
    val joinedAt: LocalDateTime,
)
