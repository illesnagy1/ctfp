package com.ctfp.domain.model

import com.ctfp.config.enum.NotificationType
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object NotificationTable : IntIdTable() {
    val title = varchar("title", 255)
    val message = text("message")
    val type = customEnumeration(
        name = "type",
        fromDb = { value -> NotificationType.valueOf(value as String) },
        toDb = { it.name }
    )
    val sendEmail = bool("send_email").default(false)
    val pushAt = timestamp("push_at").defaultExpression(CurrentTimestamp)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}
