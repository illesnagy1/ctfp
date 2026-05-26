package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object PasswordResetTokenTable : IntIdTable("password_reset_token") {
    val userId = reference("user_id", UserTable)
    val token = varchar("token", 255).uniqueIndex()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val expiresAt = timestamp("expires_at")
    val usedAt = timestamp("used_at").nullable()
}
