package com.ctfp.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class Page(
    val title: String,
    val route: String,
    val language: String = "en",
    val format: String,
    val targetBlank: Boolean = false,
    val body: String,
    val isHidden: Boolean = false,
    val isPrivate: Boolean = false,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
