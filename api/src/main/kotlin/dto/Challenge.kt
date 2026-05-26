package com.ctfp.dto

import kotlin.time.Instant
import kotlinx.serialization.Serializable


@Serializable
data class Challenge (
    val title: String,
    val description: String?,
    val isVisible: Boolean = true,
    val publishAt: Instant,
    val maxAttempts: Int,
    val categories: List<String>,
    val timeLimit: Int,
    val createdAt: Instant,
    val updatedAt: Instant
)
