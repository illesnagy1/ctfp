package com.ctfp.controller.helper

import com.ctfp.plugins.MergePatchJson
import io.ktor.openapi.AdditionalProperties
import io.ktor.openapi.JsonSchema
import io.ktor.openapi.ReferenceOr
import io.ktor.openapi.jsonSchema
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi

// OpenAPI docs for the PATCH routes' JSON Merge Patch bodies; services apply them (service/helper/MergePatch.kt).

/** Documents the body as a merge patch of [T]: the same fields and limits, all optional. */
@OptIn(ExperimentalKtorApi::class)
inline fun <reified T : Any> Route.mergePatchOf(): Route =
    describe {
        requestBody {
            required = true
            MergePatchJson { schema = jsonSchema<T>().optional() }
        }
    }

/** Bulk form of [mergePatchOf]: an object of patches keyed by id. */
@OptIn(ExperimentalKtorApi::class)
inline fun <reified T : Any> Route.mergePatchesOf(): Route =
    describe {
        requestBody {
            required = true
            MergePatchJson {
                schema =
                    jsonSchema<Map<String, T>>().copy(
                        additionalProperties = AdditionalProperties.PSchema(ReferenceOr.value(jsonSchema<T>().optional())),
                    )
            }
        }
    }

/** The schema with every field optional, named e.g. UserUpdatePatch after UserUpdateRequest. */
fun JsonSchema.optional() = copy(required = null, title = title?.removeSuffix("Request")?.plus("Patch"))
