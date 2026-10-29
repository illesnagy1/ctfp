package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import com.ctfp.domain.dao.TeamDAO
import com.ctfp.domain.dao.UserDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TeamRequest(
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.TEAM_NAME_MAX)
    val name: String,
    val ownerId: Int,
    @JsonSchema.MinLength(Limits.COUNTRY_LENGTH)
    @JsonSchema.MaxLength(Limits.COUNTRY_LENGTH)
    @JsonSchema.Description("ISO 3166-1 alpha-2 code, e.g.: HU")
    val country: String?,
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
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
