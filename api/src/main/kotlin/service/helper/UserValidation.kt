package com.ctfp.service.helper

import com.ctfp.dto.Limits
import com.ctfp.service.ApiException
import io.ktor.http.HttpStatusCode

private val USERNAME_REGEX = Regex(Limits.USERNAME_PATTERN)
private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun validateUsername(username: String) {
    if (!USERNAME_REGEX.matches(username)) {
        throw ApiException(HttpStatusCode.BadRequest, "Username must be 3-64 characters: letters, numbers, '.', '_' or '-'")
    }
}

fun validateEmail(email: String) {
    if (email.length > Limits.EMAIL_MAX || !EMAIL_REGEX.matches(email)) {
        throw ApiException(HttpStatusCode.BadRequest, "Invalid email address")
    }
}

fun validatePassword(password: String) {
    val error = when {
        password.length < Limits.PASSWORD_MIN -> "Password must be at least $Limits.PASSWORD_MIN characters"
        password.length > Limits.PASSWORD_MAX -> "Password must be at most $Limits.PASSWORD_MAX characters"
        else -> null
    }
    if (error != null) throw ApiException(HttpStatusCode.BadRequest, error)
}
