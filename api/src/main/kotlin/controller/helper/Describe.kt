package com.ctfp.controller.helper

import com.ctfp.dto.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi

/**
 * Documents the [ErrorResponse]s this route can return. They are sent by StatusPages (and the auth challenge), so
 * OpenAPI inference can't see them.
 */
@OptIn(ExperimentalKtorApi::class)
fun Route.errors(vararg statuses: HttpStatusCode): Route =
    describe {
        responses {
            for (status in statuses) {
                status {
                    description = status.description
                    schema = jsonSchema<ErrorResponse>()
                }
            }
        }
    }
