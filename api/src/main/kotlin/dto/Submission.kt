package com.ctfp.dto

import kotlinx.serialization.Serializable

/** A flag guess for the challenge in the path. */
@Serializable
data class SubmitFlagRequest(
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
