package com.ctfp.service

import io.ktor.http.HttpStatusCode

/** Thrown by services for expected failures; turned into an [com.ctfp.dto.ErrorResponse] by StatusPages. */
class ApiException(val status: HttpStatusCode, override val message: String) : RuntimeException(message)
