package com.ctfp.dto

import kotlinx.serialization.Serializable


@Serializable
data class EmailTemplate (
    val subject: String,
    val body: String
)
