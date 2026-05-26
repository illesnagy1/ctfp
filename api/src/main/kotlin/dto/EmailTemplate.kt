package com.ctfp.dto

import com.ctfp.domain.dao.EmailTemplateDAO
import kotlinx.serialization.Serializable

@Serializable
data class EmailTemplateRequest(
    val subject: String,
    val body: String
)

@Serializable
data class EmailTemplateResponse(
    val id: Int,
    val subject: String,
    val body: String
)

fun EmailTemplateDAO.apply(dto: EmailTemplateRequest) {
    subject = dto.subject
    body = dto.body
}

fun EmailTemplateDAO.toDto() = EmailTemplateResponse(
    id = id.value,
    subject = subject,
    body = body
)
