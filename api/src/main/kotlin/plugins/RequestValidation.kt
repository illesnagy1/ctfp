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
 * (and from there the frontend's forms) are also its checks; a violation is a 400 listing every failing field.
 * Services only check what annotations can't say, e.g. a taken username or a blank-but-not-empty name.
 *
 * Bodies are checked as sent: clients trim, the api doesn't.
 */
fun Application.configureRequestValidation() {
    install(RequestValidation) {
        validate {
            filter { true }
            validation { body ->
                val reasons = violations(body)
                if (reasons.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(reasons)
            }
        }
    }
}

private val json = Json

/** Limit violations in [body]: a DTO, or a map or list of them (bulk updates); other bodies have none. */
internal fun violations(body: Any): List<String> =
    when (body) {
        is Map<*, *> -> {
            body.values.flatMap { it?.let(::violations).orEmpty() }
        }

        is Iterable<*> -> {
            body.flatMap { it?.let(::violations).orEmpty() }
        }

        else -> {
            // Bodies without a serializer (strings, streams, multipart) aren't DTOs.
            @Suppress("UNCHECKED_CAST")
            val serializer = runCatching { serializer(body.javaClass) as KSerializer<Any> }.getOrNull()
            serializer?.let { violations(it.descriptor, json.encodeToJsonElement(it, body), path = "") }.orEmpty()
        }
    }

private fun violations(
    descriptor: SerialDescriptor,
    value: JsonElement,
    path: String,
): List<String> =
    when {
        value is JsonObject && descriptor.kind == StructureKind.CLASS -> {
            (0 until descriptor.elementsCount).flatMap { i ->
                val name = descriptor.getElementName(i)
                val field = value[name] ?: return@flatMap emptyList()
                val fieldPath = if (path.isEmpty()) name else "$path.$name"
                check(fieldPath, field, descriptor.getElementAnnotations(i)) +
                    violations(descriptor.getElementDescriptor(i), field, fieldPath)
            }
        }

        value is JsonArray && descriptor.kind == StructureKind.LIST -> {
            value.flatMapIndexed { i, item -> violations(descriptor.getElementDescriptor(0), item, "$path[$i]") }
        }

        else -> {
            emptyList()
        }
    }

/** Shortest form that's an address: something@something.something, no spaces. */
private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/** The annotations this checks; others (e.g. `Description`) only document. Nulls pass, nullability is the type's. */
private fun check(
    field: String,
    value: JsonElement,
    annotations: List<Annotation>,
): List<String> {
    if (value !is JsonPrimitive || value is JsonNull) return emptyList()
    val text = value.content.takeIf { value.isString }
    // Counted in code points, as JSON Schema does.
    val length = text?.let { it.codePointCount(0, it.length) }
    val number = value.doubleOrNull.takeUnless { value.isString }
    return annotations.mapNotNull { annotation ->
        when (annotation) {
            is JsonSchema.MinLength -> {
                "$field must be at least ${annotation.value} characters".takeIf { length != null && length < annotation.value }
            }

            is JsonSchema.MaxLength -> {
                "$field must be at most ${annotation.value} characters".takeIf { length != null && length > annotation.value }
            }

            is JsonSchema.Pattern -> {
                "$field must match ${annotation.value}".takeIf { text != null && !Regex(annotation.value).containsMatchIn(text) }
            }

            is JsonSchema.Format -> {
                "$field must be an email address".takeIf { annotation.value == "email" && text != null && !EMAIL.matches(text) }
            }

            is JsonSchema.Minimum -> {
                "$field must be ${if (annotation.exclusive) "more than" else "at least"} ${annotation.value.format()}".takeIf {
                    number != null && (if (annotation.exclusive) number <= annotation.value else number < annotation.value)
                }
            }

            is JsonSchema.Maximum -> {
                "$field must be ${if (annotation.exclusive) "less than" else "at most"} ${annotation.value.format()}".takeIf {
                    number != null && (if (annotation.exclusive) number >= annotation.value else number > annotation.value)
                }
            }

            else -> {
                null
            }
        }
    }
}

/** 1.0 → "1", so integer limits read as integers. */
private fun Double.format() = if (this % 1.0 == 0.0) toLong().toString() else toString()
