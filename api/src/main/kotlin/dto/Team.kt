package com.ctfp.dto

import com.ctfp.domain.dao.TeamDAO
import com.ctfp.domain.dao.UserDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TeamRequest(
    val name: String,
    val ownerId: Int,
    val country: String?,
    val website: String?,
    val isHidden: Boolean = false,
    val isBanned: Boolean = false
)

@Serializable
data class TeamResponse(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val country: String?,
    val website: String?,
    val isHidden: Boolean = false,
    val isBanned: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

fun TeamDAO.apply(dto: TeamRequest) {
    name = dto.name
    owner = UserDAO[dto.ownerId]
    country = dto.country
    website = dto.website
    isHidden = dto.isHidden
    isBanned = dto.isBanned
}

fun TeamDAO.toDto() = TeamResponse(
    id = id.value,
    name = name,
    ownerId = owner.id.value,
    country = country,
    website = website,
    isHidden = isHidden,
    isBanned = isBanned,
    createdAt = createdAt,
    updatedAt = updatedAt
)
