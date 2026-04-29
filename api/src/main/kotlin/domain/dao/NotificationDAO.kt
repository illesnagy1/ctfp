package com.ctfp.domain.dao

import com.ctfp.domain.model.NotificationTable
import com.ctfp.dto.Notification
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class NotificationDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<NotificationDAO>(NotificationTable)

    var title by NotificationTable.title
    var message by NotificationTable.message
    var type by NotificationTable.type
    var sendEmail by NotificationTable.sendEmail
    var pushAt by NotificationTable.pushAt
    var createdAt by NotificationTable.createdAt

    fun toModel() = Notification(
        title = title,
        message = message,
        type = type,
        sendEmail = sendEmail,
        pushAt = pushAt,
        createdAt = createdAt,
    )
}
