package com.ctfp.plugins

import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

/** JSON Merge Patch (RFC 7396), the body of the PATCH routes; see controller/helper/MergePatch.kt. */
val MergePatchJson = ContentType("application", "merge-patch+json")

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
        json(contentType = MergePatchJson)
    }
}
