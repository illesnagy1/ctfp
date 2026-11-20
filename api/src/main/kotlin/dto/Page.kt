package com.ctfp.dto

import com.ctfp.domain.dao.PageDAO
import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable
import java.util.Locale
import kotlin.time.Instant

@Serializable
data class PageRequest(
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.TITLE_MAX)
    val title: String,
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    val route: String,
    @JsonSchema.Pattern(Limits.LANGUAGE_TAG_PATTERN)
    @JsonSchema.MaxLength(Limits.LANGUAGE_TAG_MAX)
    val language: String = "en",
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    val format: String,
    val targetBlank: Boolean = false,
    val body: String,
    val isHidden: Boolean = false,
    val isPrivate: Boolean = false,
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
    val updatedAt: Instant,
)

fun PageDAO.apply(dto: PageRequest) {
    title = dto.title
    route = dto.route
    language = Locale.forLanguageTag(dto.language).toLanguageTag() // canonical BCP 47, e.g. pt-br → pt-BR
    format = dto.format
    targetBlank = dto.targetBlank
    body = dto.body
    isHidden = dto.isHidden
    isPrivate = dto.isPrivate
}

fun PageDAO.toDto() =
    PageResponse(
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
        updatedAt = updatedAt,
    )
