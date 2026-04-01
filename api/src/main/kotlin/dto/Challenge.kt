package com.ctfp.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class Challenge (
    val title: String,
    val description: String?,
    val isVisible: Boolean = true,
    val publishAt: LocalDateTime,
    val maxAttempts: Int,
    val categories: List<String>,
    val timeLimit: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
