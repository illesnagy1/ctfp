package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

/** When a player pressed Start on a timed challenge; their time limit counts from here. */
object ChallengeStartTable : IntIdTable("challenge_starts") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val challengeId = reference("challenge_id", ChallengeTable, onDelete = ReferenceOption.CASCADE)
    val startedAt = timestamp("started_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex("uq_challenge_starts_user_challenge", userId, challengeId)
    }
}
