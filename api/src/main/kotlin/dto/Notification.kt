package com.ctfp.dto

import com.ctfp.config.enum.NotificationType
import com.ctfp.domain.dao.NotificationDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class NotificationRequest(
    val title: String,
    val message: String,
    val type: NotificationType,
    val sendEmail: Boolean = false,
    val pushAt: Instant
)

@Serializable
data class NotificationResponse(
    val id: Int,
    val title: String,
    val message: String,
    val type: NotificationType,
    val sendEmail: Boolean = false,
    val pushAt: Instant,
    val createdAt: Instant
)

fun NotificationDAO.apply(dto: NotificationRequest) {
    title = dto.title
    message = dto.message
    type = dto.type
    sendEmail = dto.sendEmail
    pushAt = dto.pushAt
}

fun NotificationDAO.toDto() = NotificationResponse(
    id = id.value,
    title = title,
    message = message,
    type = type,
    sendEmail = sendEmail,
    pushAt = pushAt,
    createdAt = createdAt
)
