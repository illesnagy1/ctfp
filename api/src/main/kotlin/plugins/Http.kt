package com.ctfp.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.plugins.forwardedheaders.ForwardedHeaders
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.hsts.HSTS
import io.ktor.server.plugins.partialcontent.PartialContent
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.routing

private const val MAX_RANGE_COUNT = 10

fun Application.configureHttp() {
    install(Compression)
    install(ConditionalHeaders)
    install(ForwardedHeaders) // WARNING: for security, do not include this if not behind a reverse proxy
    install(XForwardedHeaders)
    install(HSTS) {
        includeSubDomains = true
    }
    install(PartialContent) {
        // Maximum number of ranges that will be accepted from a HTTP request.
        // If the HTTP request specifies more ranges, they will all be merged into a single range.
        maxRangeCount = MAX_RANGE_COUNT
    }
    if (developmentMode) {
        routing {
            // Served from the routes' `describe {}` docs, like openapi.json.
            swaggerUI(path = "swagger")
        }
    }
}
