package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object HintUnlockTable : IntIdTable("hint_unlock") {
    val hintId = reference("hint_id", HintTable)
    val userId = reference("user_id", UserTable)
    val teamId = reference("team_id", TeamTable).nullable()
    val unlockedAt = timestamp("unlocked_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex(hintId, userId)
        uniqueIndex(hintId, teamId)
    }
}
