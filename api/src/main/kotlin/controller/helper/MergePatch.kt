package com.ctfp.controller.helper

import com.ctfp.plugins.MergePatchJson
import com.ctfp.plugins.violations
import com.ctfp.service.ApiException
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.AdditionalProperties
import io.ktor.openapi.JsonSchema
import io.ktor.openapi.ReferenceOr
import io.ktor.openapi.jsonSchema
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

// A cleared (removed) nullable member decodes as null; a required one is then missing, and that's a 400.
private val patchJson = Json { explicitNulls = false }

/** RFC 7396 §2: objects merge member by member, `null` removes a member, any other value replaces it. */
fun JsonElement.mergePatch(patch: JsonElement): JsonElement {
    if (patch !is JsonObject) return patch
    val merged = (this as? JsonObject).orEmpty().toMutableMap()
    for ((key, value) in patch) {
        if (value is JsonNull) merged.remove(key) else merged[key] = (merged[key] ?: JsonNull).mergePatch(value)
    }
    return JsonObject(merged)
}

/** [current] with [patch] applied; unknown members and cleared required ones are a 400. Check it with [validated]. */
fun <T : Any> patched(
    serializer: KSerializer<T>,
    current: T,
    patch: JsonElement,
): T {
    val merged = patchJson.encodeToJsonElement(serializer, current).mergePatch(patch)
    val result =
        try {
            patchJson.decodeFromJsonElement(serializer, merged)
        } catch (e: SerializationException) {
            throw ApiException(HttpStatusCode.BadRequest, "Invalid patch: ${e.message}")
        }
    return result
}

/** [body] if it keeps its DTOs' limits, else the same 400 a received body gets (pointers into [body]; see RequestValidation.kt). */
fun <T : Any> validated(body: T): T {
    val violations = violations(body)
    if (violations.isNotEmpty()) throw RequestValidationException(body, violations.map { it.message })
    return body
}

/**
 * [current] with the request's merge patch applied (`application/merge-patch+json`; plain JSON works too).
 * shortcut: read, patch and write aren't one transaction, so two patches racing on one record can lose one's other
 * fields; lock the row around them if these stop being rare admin and profile edits.
 */
suspend inline fun <reified T : Any> ApplicationCall.receivePatched(current: T): T =
    validated(patched(serializer<T>(), current, receive<JsonObject>()))

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
