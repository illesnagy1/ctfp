package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object HintTable : IntIdTable() {
    val challengeId = reference("challenge_id", ChallengeTable, onDelete = ReferenceOption.CASCADE)
    val body = text("body")
    val cost = integer("cost").default(0)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}
