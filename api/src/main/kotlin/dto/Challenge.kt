package com.ctfp.dto

import com.ctfp.domain.dao.ChallengeDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeRequest(
    val title: String,
    val description: String?,
    val isVisible: Boolean = true,
    val publishAt: Instant,
    val maxAttempts: Int,
    val categories: List<String>,
    val timeLimit: Int
)

@Serializable
data class ChallengeResponse(
    val id: Int,
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

fun ChallengeDAO.apply(dto: ChallengeRequest) {
    title = dto.title
    description = dto.description
    isVisible = dto.isVisible
    publishAt = dto.publishAt
    maxAttempts = dto.maxAttempts
    categories = dto.categories
    timeLimit = dto.timeLimit
}

fun ChallengeDAO.toDto() = ChallengeResponse(
    id = id.value,
    title = title,
    description = description,
    isVisible = isVisible,
    publishAt = publishAt,
    maxAttempts = maxAttempts,
    categories = categories,
    timeLimit = timeLimit,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
