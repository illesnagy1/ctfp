package com.ctfp

import com.ctfp.plugins.configureRouting
import com.ctfp.plugins.configureSecurity
import com.typesafe.config.ConfigFactory
import io.ktor.openapi.OpenApiDoc
import io.ktor.openapi.OpenApiInfo
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.routing.openapi.OpenApiDocSource
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import java.io.File

private val json = Json {
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
            config = HoconApplicationConfig(ConfigFactory.load().withOnlyPath("jwt"))
        }
        application {
            configureSecurity()
            configureRouting()
        }
        startApplication()

        val doc = OpenApiDocSource.Routing(
            serializeModel = { json.encodeToString(OpenApiDoc.serializer(), it) }
        ).read(application, OpenApiDoc(info = OpenApiInfo("CTFP API", "1.0.0")))

        output.writeText(doc.content)
        println("Wrote ${output.path}")
    }
}
