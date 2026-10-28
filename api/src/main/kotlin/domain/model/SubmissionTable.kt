package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object SubmissionTable : IntIdTable() {
    val userId = reference("user_id", UserTable)
    val teamId = optReference("team_id", TeamTable)
    val challengeId = reference("challenge_id", ChallengeTable, onDelete = ReferenceOption.CASCADE)
    val flagId = optReference("flag_id", FlagTable, onDelete = ReferenceOption.SET_NULL)
    /** What was submitted, trimmed; kept for wrong guesses too, so admins can review them. */
    val submittedFlag = text("submitted_flag")
    /** Its own column: [flagId] goes null when the matched flag is deleted, but the solve still counts. */
    val isCorrect = bool("is_correct")
    val submittedAt = timestamp("submitted_at").defaultExpression(CurrentTimestamp)

    init {
        index("idx_submissions_user_challenge", false, userId, challengeId)
    }
}
