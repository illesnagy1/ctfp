package com.ctfp.service.helper

import com.ctfp.service.ApiException
import io.ktor.http.HttpStatusCode

const val MIN_PASSWORD_LENGTH = 8
const val MAX_PASSWORD_LENGTH = 128
private val USERNAME_REGEX = Regex("^[A-Za-z0-9._-]{3,64}$")
private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

fun validateUsername(username: String) {
    if (!USERNAME_REGEX.matches(username)) {
        throw ApiException(HttpStatusCode.BadRequest, "Username must be 3-64 characters: letters, numbers, '.', '_' or '-'")
    }
}

fun validateEmail(email: String) {
    if (email.length > 255 || !EMAIL_REGEX.matches(email)) {
        throw ApiException(HttpStatusCode.BadRequest, "Invalid email address")
    }
}

fun validatePassword(password: String) {
    val error = when {
        password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        password.length > MAX_PASSWORD_LENGTH -> "Password must be at most $MAX_PASSWORD_LENGTH characters"
        else -> null
    }
    if (error != null) throw ApiException(HttpStatusCode.BadRequest, error)
}
