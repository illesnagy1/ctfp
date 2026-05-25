package com.ctfp.repository

import com.ctfp.dto.Notification


interface INotificationRepository {
    suspend fun getNotification(id: Int): Notification
    suspend fun getAllNotifications(): List<Notification>
    suspend fun createNotification(template: Notification): Int
    suspend fun updateNotification(id: Int, notification: Notification)
    suspend fun deleteNotification(id: Int)
}
