package com.ctfp.dto

import com.ctfp.domain.dao.PageDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PageRequest(
    val title: String,
    val route: String,
    val language: String = "en",
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
