package com.ctfp.dto

import com.ctfp.domain.dao.MagicLinkDAO
import com.ctfp.domain.dao.UserDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MagicLinkRequest(
    val userId: Int,
    val token: String,
    val expiresAt: Instant
)

@Serializable
data class MagicLinkResponse(
    val id: Int,
    val userId: Int,
    val token: String,
    val createdAt: Instant,
    val expiresAt: Instant,
    val usedAt: Instant?
)

fun MagicLinkDAO.apply(dto: MagicLinkRequest) {
    user = UserDAO[dto.userId]
    token = dto.token
    expiresAt = dto.expiresAt
}

fun MagicLinkDAO.toDto() = MagicLinkResponse(
    id = id.value,
    userId = user.id.value,
    token = token,
    createdAt = createdAt,
    expiresAt = expiresAt,
    usedAt = usedAt
)
