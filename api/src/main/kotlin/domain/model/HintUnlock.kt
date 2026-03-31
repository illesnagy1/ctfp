package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.*

object HintUnlockTable : IntIdTable("hint_unlock") {
    val hintId = reference("hint_id", HintTable)
    val userId = reference("user_id", UserTable)
    val teamId = reference("team_id", TeamTable).nullable()
    val unlockedAt = datetime("unlocked_at").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex(hintId, userId)
        uniqueIndex(hintId, teamId)
    }
}
