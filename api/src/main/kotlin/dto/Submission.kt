package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** A flag guess for the challenge in the path. */
@Serializable
data class SubmitFlagRequest(
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.SUBMITTED_FLAG_MAX)
    val flag: String,
)

@Serializable
data class SubmissionResult(
    val correct: Boolean,
    /** Already solved before this guess; nothing was recorded. */
    val alreadySolved: Boolean = false,
    /** Wrong guesses left, when the challenge limits them; null when it doesn't or the guess was right. */
    val attemptsLeft: Int? = null,
)

/** A recorded guess: everyone's for admins, only their own for players. */
@Serializable
data class SubmissionResponse(
    val id: Int,
    val challengeId: Int,
    val challengeTitle: String,
    val userId: Int,
    val username: String,
    val submittedFlag: String,
    val isCorrect: Boolean,
    val submittedAt: Instant,
    @JsonSchema.Description("The submitter's IP address; null for guesses recorded before it was kept.")
    val ipAddress: String?,
    @JsonSchema.Description("The submitter's browser, as it reported itself; null when it didn't.")
    val userAgent: String?,
)
