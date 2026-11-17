package com.ctfp.plugins

import io.ktor.openapi.JsonSchema
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.serializer

/**
 * Enforces the `@JsonSchema` limits on every received body, so the annotations that document a DTO in openapi.json
 * (and from there the frontend's forms) are also its checks; a violation is a 400 whose problem details list every
 * failing field (see StatusPages.kt).
 * Services only check what annotations can't say, e.g. a taken username or a blank-but-not-empty name.
 *
 * Bodies are checked as sent: clients trim, the api doesn't.
 */
fun Application.configureRequestValidation() {
    install(RequestValidation) {
        validate {
            filter { true }
            validation { body ->
                val violations = violations(body)
                if (violations.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(violations.map { it.message })
            }
        }
    }
}

private val json = Json

/** A broken limit: where in the body (e.g. `items`, `1`, `name`) and which, e.g. `must be at most 3 characters`. */
internal data class Violation(
    val path: List<String>,
    val rule: String,
) {
    /** E.g. `items[1].name must be at most 3 characters`. */
    val message: String
        get() {
            val field =
                path.withIndex().joinToString("") { (i, key) ->
                    when {
                        key.all(Char::isDigit) -> "[$key]"
                        i == 0 -> key
                        else -> ".$key"
                    }
                }
            return "$field $rule"
        }

    /** The field as an RFC 6901 JSON Pointer in a URI fragment, e.g. `#/items/1/name`. */
    val pointer: String
        get() = "#/" + path.joinToString("/") { it.replace("~", "~0").replace("/", "~1") }
}

/** Limit violations in [body]: a DTO, or a map or list of them (bulk updates); other bodies have none. */
internal fun violations(
    body: Any,
    path: List<String> = emptyList(),
): List<Violation> =
    when (body) {
        is Map<*, *> -> {
            body.entries.flatMap { (key, value) -> value?.let { violations(it, path + key.toString()) }.orEmpty() }
        }

        is Iterable<*> -> {
            body.flatMapIndexed { i, item -> item?.let { violations(it, path + i.toString()) }.orEmpty() }
        }

        else -> {
            // Bodies without a serializer (strings, streams, multipart) aren't DTOs.
            @Suppress("UNCHECKED_CAST")
            val serializer = runCatching { serializer(body.javaClass) as KSerializer<Any> }.getOrNull()
            serializer?.let { violations(it.descriptor, json.encodeToJsonElement(it, body), path) }.orEmpty()
        }
    }

private fun violations(
    descriptor: SerialDescriptor,
    value: JsonElement,
    path: List<String>,
): List<Violation> =
    when {
        value is JsonObject && descriptor.kind == StructureKind.CLASS -> {
            (0 until descriptor.elementsCount).flatMap { i ->
                val name = descriptor.getElementName(i)
                val field = value[name] ?: return@flatMap emptyList()
                val fieldPath = path + name
                check(fieldPath, field, descriptor.getElementAnnotations(i)) +
                    violations(descriptor.getElementDescriptor(i), field, fieldPath)
            }
        }

        value is JsonArray && descriptor.kind == StructureKind.LIST -> {
            value.flatMapIndexed { i, item -> violations(descriptor.getElementDescriptor(0), item, path + i.toString()) }
        }

        else -> {
            emptyList()
        }
    }

/** Shortest form that's an address: something@something.something, no spaces. */
private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** The annotations this checks; others (e.g. `Description`) only document. Nulls pass, nullability is the type's. */
private fun check(
    field: List<String>,
    value: JsonElement,
    annotations: List<Annotation>,
): List<Violation> {
    if (value !is JsonPrimitive || value is JsonNull) return emptyList()
    val text = value.content.takeIf { value.isString }
    // Counted in code points, as JSON Schema does.
    val length = text?.let { it.codePointCount(0, it.length) }
    val number = value.doubleOrNull.takeUnless { value.isString }
    val rules =
        annotations.mapNotNull { annotation ->
            when (annotation) {
                is JsonSchema.MinLength -> {
                    "must be at least ${annotation.value} characters".takeIf { length != null && length < annotation.value }
                }

                is JsonSchema.MaxLength -> {
                    "must be at most ${annotation.value} characters".takeIf { length != null && length > annotation.value }
                }

                is JsonSchema.Pattern -> {
                    "must match ${annotation.value}".takeIf { text != null && !Regex(annotation.value).containsMatchIn(text) }
                }

                is JsonSchema.Format -> {
                    "must be an email address".takeIf { annotation.value == "email" && text != null && !EMAIL.matches(text) }
                }

                is JsonSchema.Minimum -> {
                    "must be ${if (annotation.exclusive) "more than" else "at least"} ${annotation.value.format()}".takeIf {
                        number != null && (if (annotation.exclusive) number <= annotation.value else number < annotation.value)
                    }
                }

                is JsonSchema.Maximum -> {
                    "must be ${if (annotation.exclusive) "less than" else "at most"} ${annotation.value.format()}".takeIf {
                        number != null && (if (annotation.exclusive) number >= annotation.value else number > annotation.value)
                    }
                }

                else -> {
                    null
                }
            }
        }
    return rules.map { Violation(field, it) }
}

/** 1.0 → "1", so integer limits read as integers. */
private fun Double.format() = if (this % 1.0 == 0.0) toLong().toString() else toString()
