package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object TeamMemberTable : IntIdTable() {
    val teamId = reference("team_id", TeamTable)
    val userId = reference("user_id", UserTable)
    val joinedAt = timestamp("joined_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex(teamId, userId)
    }
}
