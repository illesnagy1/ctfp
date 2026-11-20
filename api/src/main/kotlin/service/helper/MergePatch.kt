package com.ctfp.service.helper

import com.ctfp.plugins.violations
import com.ctfp.service.ApiException
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

// JSON Merge Patch (RFC 7396) for the PATCH routes. Services apply it to the record's current state inside the
// transaction that writes it, with the row locked, so concurrent patches to one record apply one after another.

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
    return try {
        patchJson.decodeFromJsonElement(serializer, merged)
    } catch (e: SerializationException) {
        throw ApiException(HttpStatusCode.BadRequest, "Invalid patch: ${e.message}", e)
    }
}

/** [body] if it keeps its DTOs' limits, else the 400 a received body gets (pointers into [body]; see RequestValidation.kt). */
fun <T : Any> validated(body: T): T {
    val violations = violations(body)
    if (violations.isNotEmpty()) throw RequestValidationException(body, violations.map { it.message })
    return body
}
