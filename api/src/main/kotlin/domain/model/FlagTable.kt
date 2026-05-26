package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object FlagTable : IntIdTable() {
    val challengeId = reference("challenge_id", ChallengeTable)
    val pattern = varchar("pattern", 255).uniqueIndex()
    val isCaseSensitive = bool("is_case_sensitive")
    val baseValue = integer("base_value")
    val decayValue = integer("decay_value").nullable()
    val minValue = integer("min_value").nullable()
    val decayFun = varchar("decay_fun", 50).default("static")
    val sortOrder = integer("sort_order").default(1)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}
