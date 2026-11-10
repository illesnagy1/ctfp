package com.ctfp.plugins

import com.ctfp.dto.ErrorResponse
import com.ctfp.service.ApiException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.response.respond
import org.jetbrains.exposed.v1.dao.exceptions.EntityNotFoundException
import java.sql.SQLException

/** SQLSTATE for a unique constraint violation, the same in PostgreSQL and H2. */
private const val UNIQUE_VIOLATION = "23505"

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(cause.status, ErrorResponse(cause.message))
        }
        exception<EntityNotFoundException> { call, _ ->
            call.respond(HttpStatusCode.NotFound, ErrorResponse("Not found"))
        }
        exception<BadRequestException> { call, _ ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Malformed request"))
        }
        // Exposed wraps driver errors in ExposedSQLException, a SQLException that carries the original SQLSTATE.
        exception<SQLException> { call, cause ->
            if (cause.sqlState == UNIQUE_VIOLATION) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Already exists"))
            } else {
                call.respondInternalError(cause)
            }
        }
        exception<Throwable> { call, cause ->
            call.respondInternalError(cause)
        }
    }
}

private suspend fun ApplicationCall.respondInternalError(cause: Throwable) {
    application.log.error("Unhandled exception", cause)
    respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal server error"))
}
