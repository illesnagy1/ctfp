package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object ChallengeTable : IntIdTable("challenge") {
    val title = varchar("title", 255).uniqueIndex()
    val description = text("description").nullable()
    val isVisible = bool("is_visible").default(true)
    val publishAt = timestamp("publish_at").defaultExpression(CurrentTimestamp)
    val maxAttempts = integer("max_attempts")
    val categories = array<String>("categories")
    val timeLimit = integer("time_limit")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
