package com.ctfp.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable


@Serializable
data class Team(
    val name: String,
    val ownerId: Int,
    val country: String?,
    val website: String?,
    val isHidden: Boolean = false,
    val isBanned: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
)
