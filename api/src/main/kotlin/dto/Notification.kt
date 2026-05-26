package com.ctfp.dto

import com.ctfp.config.enum.NotificationType
import kotlin.time.Instant
import kotlinx.serialization.Serializable


@Serializable
data class Notification(
    val title: String,
    val message: String,
    val type: NotificationType,
    val sendEmail: Boolean = false,
    val pushAt: Instant,
    val createdAt: Instant
)
