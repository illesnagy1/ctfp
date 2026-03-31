package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.*

object TeamMemberTable : IntIdTable("team_member") {
    val teamId = reference("team_id", TeamTable)
    val userId = reference("user_id", UserTable)
    val joinedAt = datetime("joined_at").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex(teamId, userId)
    }
}
