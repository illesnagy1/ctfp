package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable

/**
 * The final standings in CTFtime's scoreboard feed format (https://ctftime.org/json-scoreboard-feed), which CTFtime
 * polls to import results. Only the stable fields; per-task stats wait for CTFtime to finalize the format.
 */
@Serializable
data class CtftimeFeed(
    val standings: List<CtftimeStanding>,
)

@Serializable
data class CtftimeStanding(
    val pos: Int,
    @JsonSchema.Description("The player's username; teams aren't scored yet.")
    val team: String,
    val score: Int,
    @JsonSchema.Description("Unix seconds of the last accepted flag; absent without solves.")
    val lastAccept: Long? = null,
)

fun List<ScoreboardEntry>.toCtftimeFeed() =
    CtftimeFeed(
        mapIndexed { i, entry ->
            CtftimeStanding(pos = i + 1, team = entry.username, score = entry.points, lastAccept = entry.lastSolveAt?.epochSeconds)
        },
    )
