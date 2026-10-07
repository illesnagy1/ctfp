package com.ctfp.controller.helper

import com.ctfp.dto.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.describe

/**
 * Documents the [ErrorResponse]s this route can return. They are sent by StatusPages (and the auth challenge), so
 * OpenAPI inference can't see them.
 */
fun Route.errors(vararg statuses: HttpStatusCode): Route = describe {
    responses {
        for (status in statuses) {
            status {
                description = status.description
                schema = jsonSchema<ErrorResponse>()
            }
        }
    }
}
