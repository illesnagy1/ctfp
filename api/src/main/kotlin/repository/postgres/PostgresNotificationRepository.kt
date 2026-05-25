package com.ctfp.repository.postgres

import com.ctfp.domain.dao.NotificationDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.Notification
import com.ctfp.repository.INotificationRepository


class PostgresNotificationRepository : INotificationRepository {
    override suspend fun getNotification(id: Int): Notification = withTransaction {
        NotificationDAO[id].toModel()
    }

    override suspend fun getAllNotifications(): List<Notification> = withTransaction {
        NotificationDAO.all().map { it.toModel() }
    }

    override suspend fun createNotification(template: Notification): Int = withTransaction {
        val newNotification = NotificationDAO.new {
            title = template.title
            message = template.message
            type = template.type
            sendEmail = template.sendEmail
            pushAt = template.pushAt
            createdAt = template.createdAt
        }
        newNotification.id.value
    }

    override suspend fun updateNotification(id: Int, notification: Notification) = withTransaction {
        val dbNotification = NotificationDAO[id]
        dbNotification.run {
            title = notification.title
            message = notification.message
            type = notification.type
            sendEmail = notification.sendEmail
            pushAt = notification.pushAt
            createdAt = notification.createdAt
        }
    }

    override suspend fun deleteNotification(id: Int) = withTransaction {
        NotificationDAO[id].delete()
    }
}
