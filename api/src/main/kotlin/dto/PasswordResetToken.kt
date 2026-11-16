package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequest(
    @JsonSchema.MaxLength(Limits.EMAIL_MAX)
    val email: String,
)

@Serializable
data class ResetPasswordRequest(
    @JsonSchema.Description("From the link in the reset email.")
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    val token: String,
    @JsonSchema.MinLength(Limits.PASSWORD_MIN)
    @JsonSchema.MaxLength(Limits.PASSWORD_MAX)
    val password: String,
)
