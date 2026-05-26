package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object SubmissionTable : IntIdTable("submission") {
    val userId = reference("user_id", UserTable)
    val teamId = reference("team_id", TeamTable).nullable()
    val challengeId = reference("challenge_id", ChallengeTable)
    val flagId = reference("flag_id", FlagTable).nullable()
    val submittedAt = timestamp("submitted_at").defaultExpression(CurrentTimestamp)
}
