package com.ctfp.repository.postgres

import com.ctfp.dto.Notification
import com.ctfp.repository.NotificationRepository


class PostgresNotificationRepository : NotificationRepository {
    override suspend fun getNotification(id: Int): Notification {
        TODO("Not yet implemented")
    }

    override suspend fun getAllNotifications(): List<Notification> {
        TODO("Not yet implemented")
    }

    override suspend fun createNotification(template: Notification): Int {
        TODO("Not yet implemented")
    }

    override suspend fun updateNotification(id: Int, notification: Notification) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteNotification(id: Int) {
        TODO("Not yet implemented")
    }
}
