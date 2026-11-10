package com.ctfp.dto

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** One player on the scoreboard; the list comes ranked, best first. */
@Serializable
data class ScoreboardEntry(
    val userId: Int,
    val username: String,
    val country: String?,
    val solves: Int,
    val points: Int,
    /** When the player reached their score; breaks ties (earlier ranks higher). Null without solves. */
    val lastSolveAt: Instant?,
)
