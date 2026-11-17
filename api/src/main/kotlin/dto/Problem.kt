package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable

/**
 * An error response, as RFC 9457 problem details (`application/problem+json`). [type] stays `about:blank`: the status
 * says what kind of problem it is, [title] is its standard phrase and [detail] what went wrong this time.
 */
@Serializable
data class ProblemDetails(
    @JsonSchema.Description("A URI for the kind of problem; about:blank means the status says it all.")
    val type: String = "about:blank",
    @JsonSchema.Description("The status's standard phrase, e.g. Not Found.")
    val title: String,
    val status: Int,
    @JsonSchema.Description("What went wrong this time, for people to read.")
    val detail: String? = null,
    @JsonSchema.Description("The request's path.")
    val instance: String? = null,
    @JsonSchema.Description("The body's fields that broke a limit; only on 400s from request validation.")
    val errors: List<FieldProblem>? = null,
)

/** One field of the request body that broke a limit, in the shape RFC 9457's example suggests. */
@Serializable
data class FieldProblem(
    @JsonSchema.Description("JSON Pointer (RFC 6901) to the field, as a fragment, e.g. #/username or #/items/1/name.")
    val pointer: String,
    val detail: String,
)
