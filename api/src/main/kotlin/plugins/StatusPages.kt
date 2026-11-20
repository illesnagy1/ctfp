package com.ctfp.plugins

import com.ctfp.dto.FieldProblem
import com.ctfp.dto.ProblemDetails
import com.ctfp.service.ApiException
import com.ctfp.service.helper.audit
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respondText
import org.jetbrains.exposed.v1.dao.exceptions.EntityNotFoundException
import org.slf4j.event.Level
import kotlinx.serialization.json.Json
import java.sql.SQLException

/** SQLSTATE for a unique constraint violation, the same in PostgreSQL and H2. */
private const val UNIQUE_VIOLATION = "23505"

/** Absent members are left out, rather than sent as null; `type` is always written. */
private val problemJson =
    Json {
        explicitNulls = false
        encodeDefaults = true
    }

/** Answers with RFC 9457 problem details (`application/problem+json`). */
suspend fun ApplicationCall.respondProblem(
    status: HttpStatusCode,
    detail: String? = null,
    errors: List<FieldProblem>? = null,
) {
    val problem =
        ProblemDetails(title = status.description, status = status.value, detail = detail, instance = request.path(), errors = errors)
    respondText(problemJson.encodeToString(problem), ContentType.Application.ProblemJson, status)
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respondProblem(cause.status, cause.message)
        }
        exception<EntityNotFoundException> { call, _ ->
            call.respondProblem(HttpStatusCode.NotFound, "Not found")
        }
        // The body broke a DTO's @JsonSchema limits; each field is listed, see RequestValidation.kt.
        exception<RequestValidationException> { call, cause ->
            val violations = violations(cause.value)
            audit("input_validation_fail", "path" to call.request.path(), "fields" to violations.joinToString(" ") { it.pointer })
            call.respondProblem(
                HttpStatusCode.BadRequest,
                cause.reasons.joinToString("; "),
                violations.map { FieldProblem(it.pointer, it.message) },
            )
        }
        exception<BadRequestException> { call, _ ->
            call.respondProblem(HttpStatusCode.BadRequest, "Malformed request")
        }
        // Exposed wraps driver errors in ExposedSQLException, a SQLException that carries the original SQLSTATE.
        exception<SQLException> { call, cause ->
            if (cause.sqlState == UNIQUE_VIOLATION) {
                call.respondProblem(HttpStatusCode.Conflict, "Already exists")
            } else {
                call.respondInternalError(cause)
            }
        }
        exception<Throwable> { call, cause ->
            call.respondInternalError(cause)
        }
        // Errors Ktor answers itself without a body, e.g. no route for the path; problem details already sent stay.
        status(HttpStatusCode.NotFound, HttpStatusCode.MethodNotAllowed, HttpStatusCode.UnsupportedMediaType) { status ->
            if (content.contentType?.match(ContentType.Application.ProblemJson) != true) call.respondProblem(status)
        }
        // From RateLimit, which has set Retry-After (RateLimiting.kt).
        status(HttpStatusCode.TooManyRequests) { status ->
            audit("malicious_excess_use", "path" to call.request.path(), level = Level.WARN)
            if (content.contentType?.match(ContentType.Application.ProblemJson) != true) {
                val wait = call.response.headers[HttpHeaders.RetryAfter]
                call.respondProblem(status, wait?.let { "Too many requests, try again in $it seconds" })
            }
        }
    }
}

private suspend fun ApplicationCall.respondInternalError(cause: Throwable) {
    application.log.error("Unhandled exception", cause)
    respondProblem(HttpStatusCode.InternalServerError)
}
