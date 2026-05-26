package com.ctfp.service

import com.ctfp.domain.dao.NotificationDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.NotificationRequest
import com.ctfp.dto.NotificationResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto


class NotificationService {
    suspend fun getNotification(id: Int): NotificationResponse = withTransaction {
        NotificationDAO[id].toDto()
    }

    suspend fun getAllNotifications(): List<NotificationResponse> = withTransaction {
        NotificationDAO.all().map { it.toDto() }
    }

    suspend fun createNotification(template: NotificationRequest): Int = withTransaction {
        val newNotification = NotificationDAO.new {
            apply(template)
        }
        newNotification.id.value
    }

    suspend fun updateNotification(id: Int, notification: NotificationRequest) = withTransaction {
        NotificationDAO.findByIdAndUpdate(id) {
            it.apply(notification)
        }
    }

    suspend fun deleteNotification(id: Int) = withTransaction {
        NotificationDAO[id].delete()
    }
}
