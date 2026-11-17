package com.ctfp

import com.ctfp.plugins.configureRateLimiting
import com.ctfp.plugins.configureRouting
import com.ctfp.plugins.configureSecurity
import com.typesafe.config.ConfigFactory
import io.ktor.openapi.OpenApiDoc
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

private val json =
    Json {
        prettyPrint = true
        explicitNulls = false
    }

/**
 * Writes the OpenAPI documentation to the given path. Run with `./gradlew generateOpenApi`.
 */
fun main(args: Array<String>) {
    val output = File(args.single())

    testApplication {
        environment {
            // Only what security and routing read while being set up; nothing here connects to a database.
            val loaded = ConfigFactory.load()
            config =
                HoconApplicationConfig(
                    listOf("jwt", "mail", "rateLimit", "settings", "uploads")
                        .map(loaded::withOnlyPath)
                        .reduce { merged, path -> merged.withFallback(path) },
                )
        }
        application {
            configureSecurity()
            configureRateLimiting()
            configureRouting()
        }
        startApplication()

        val doc =
            OpenApiDocSource
                .Routing(
                    serializeModel = { json.encodeToString(OpenApiDoc.serializer(), it) },
                ).read(application, OpenApiDoc(info = OpenApiInfo("CTFP API", "1.0.0")))

        checkOperationIds(doc.content)
        output.writeText(doc.content)
        println("Wrote ${output.path}")
    }
}

/** Every documented route needs its own operationId: the frontend's generated functions are named after them. */
private fun checkOperationIds(content: String) {
    val paths =
        json
            .parseToJsonElement(content)
            .jsonObject
            .getValue("paths")
            .jsonObject
    val routesById = mutableMapOf<String, MutableList<String>>()
    for ((path, item) in paths) {
        for ((method, operation) in item.jsonObject) {
            if (operation !is JsonObject) continue
            val route = "${method.uppercase()} $path"
            val id = checkNotNull(operation["operationId"]) { "$route has no operationId; add .operation(...) or .hide()" }
            routesById.getOrPut(id.jsonPrimitive.content) { mutableListOf() } += route
        }
    }
    val duplicates = routesById.filterValues { it.size > 1 }
    check(duplicates.isEmpty()) { "Duplicate operationIds: $duplicates" }
}
