package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.*

object PasswordResetTokenTable : IntIdTable("password_reset_token") {
    val userId = reference("user_id", UserTable)
    val token = varchar("token", 255).uniqueIndex()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val expiresAt = datetime("expires_at")
    val usedAt = datetime("used_at").nullable()
}
