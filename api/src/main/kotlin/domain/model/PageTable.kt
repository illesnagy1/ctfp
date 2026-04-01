package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.*

object PageTable : IntIdTable("page") {
    val title = varchar("title", 255)
    val route = varchar("route", 255)
    val language = varchar("language", 2).default("en")
    val format = varchar("format", 255)
    val targetBlank = bool("target_blank").default(false)
    val body = text("body")
    val isHidden = bool("is_hidden").default(false)
    val isPrivate = bool("is_protected").default(false)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex(route, language)
    }
}
