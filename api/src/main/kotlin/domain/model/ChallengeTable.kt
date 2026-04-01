package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.*

object ChallengeTable : IntIdTable("challenge") {
    val title = varchar("title", 255).uniqueIndex()
    val description = text("description").nullable()
    val isVisible = bool("is_visible").default(true)
    val publishAt = datetime("publish_at").defaultExpression(CurrentDateTime)
    val maxAttempts = integer("max_attempts")
    val categories = array<String>("categories")
    val timeLimit = integer("time_limit")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}
