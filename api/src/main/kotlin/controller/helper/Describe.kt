package com.ctfp.controller.helper

import com.ctfp.dto.ProblemDetails
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi

/**
 * Names this route's operation ([id] becomes the generated client's function name) and documents the errors it can
 * return, as [ProblemDetails] (RFC 9457), plus the 500 any route can. They are sent by StatusPages (and the auth
 * challenge), so OpenAPI inference can't see them.
 */
@OptIn(ExperimentalKtorApi::class)
fun Route.operation(
    id: String,
    vararg errors: HttpStatusCode,
): Route =
    describe {
        operationId = id
        responses {
            for (status in errors.toList() + HttpStatusCode.InternalServerError) {
                status {
                    description = status.description
                    ContentType.Application.ProblemJson {
                        schema = jsonSchema<ProblemDetails>()
                    }
                }
            }
        }
    }
