package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import com.ctfp.domain.dao.ChallengeDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChallengeRequest(
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.TITLE_MAX)
    val title: String,
    @JsonSchema.Description("Markdown; inline HTML is sanitized when shown.")
    val description: String?,
    val isVisible: Boolean = true,
    val publishAt: Instant,
    @JsonSchema.Description("Wrong guesses allowed per player; 0 means no limit.")
    @JsonSchema.Minimum(0.0)
    val maxAttempts: Int,
    val categories: List<String>,
    @JsonSchema.Description("Seconds to solve after starting; 0 means no limit.")
    @JsonSchema.Minimum(0.0)
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
