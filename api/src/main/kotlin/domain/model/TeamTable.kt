package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.*

object TeamTable : IntIdTable("team") {
    val name = varchar("name", 64).uniqueIndex()
    val ownerId = reference("owner_id", UserTable)
    val country = varchar("country", 2).nullable()
    val website = varchar("website", 255).nullable()
    val isHidden = bool("is_hidden").default(false)
    val isBanned = bool("is_banned").default(false)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}
