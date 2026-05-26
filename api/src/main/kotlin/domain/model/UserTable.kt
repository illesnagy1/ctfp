package com.ctfp.domain.model

import com.ctfp.config.enum.Language
import com.ctfp.config.enum.UserRole
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object UserTable : IntIdTable() {
    val codeId = reference("code_id", RegistrationCodeTable)
    val username = varchar("username", 64).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val language = customEnumeration(
        "language",
        fromDb = { value -> Language.entries.first { it.code == value as String } },
        toDb = { it.code }
    )
    val country = varchar("country", 2).nullable()
    val website = varchar("website", 255).nullable()
    val role = customEnumeration(
        "role",
        fromDb = { value -> UserRole.valueOf(value as String) },
        toDb = { it.name }
    )
    val isVerified = bool("is_verified").default(false)
    val isHidden = bool("is_hidden").default(false)
    val isBanned = bool("is_banned").default(false)
    val totpSecret = varchar("totp_secret", 255).nullable()
    val totpEnabled = bool("totp_enabled").default(false)
    val soundEnabled = bool("sound_enabled").default(true)
    // TODO: JSON
    val visibilitySettings = text("visibility_settings").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
