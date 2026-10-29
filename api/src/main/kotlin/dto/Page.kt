package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import com.ctfp.domain.dao.PageDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PageRequest(
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.TITLE_MAX)
    val title: String,
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    val route: String,
    @JsonSchema.MinLength(Limits.PAGE_LANGUAGE_LENGTH)
    @JsonSchema.MaxLength(Limits.PAGE_LANGUAGE_LENGTH)
    val language: String = "en",
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    val format: String,
    val targetBlank: Boolean = false,
    val body: String,
    val isHidden: Boolean = false,
    val isPrivate: Boolean = false
)

@Serializable
data class PageResponse(
    val id: Int,
    val title: String,
    val route: String,
    val language: String = "en",
    val format: String,
    val targetBlank: Boolean = false,
    val body: String,
    val isHidden: Boolean = false,
    val isPrivate: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

fun PageDAO.apply(dto: PageRequest) {
    title = dto.title
    route = dto.route
    language = dto.language
    format = dto.format
    targetBlank = dto.targetBlank
    body = dto.body
    isHidden = dto.isHidden
    isPrivate = dto.isPrivate
}

fun PageDAO.toDto() = PageResponse(
    id = id.value,
    title = title,
    route = route,
    language = language,
    format = format,
    targetBlank = targetBlank,
    body = body,
    isHidden = isHidden,
    isPrivate = isPrivate,
    createdAt = createdAt,
    updatedAt = updatedAt
)
