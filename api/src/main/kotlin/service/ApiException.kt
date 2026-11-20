package com.ctfp.service

import io.ktor.http.HttpStatusCode

/** Thrown by services for expected failures; StatusPages answers with problem details, [message] as their `detail`. */
class ApiException(
    val status: HttpStatusCode,
    override val message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
