package com.ctfp.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class Team(
    val name: String,
    val ownerId: Int,
    val country: String?,
    val website: String?,
    val isHidden: Boolean = false,
    val isBanned: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
