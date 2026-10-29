package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable

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
