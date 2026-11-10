package com.ctfp.plugins

import io.github.flaxoos.ktor.server.plugins.ratelimiter.RateLimiting
import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.TokenBucket
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlin.time.Duration.Companion.seconds

private const val RATE_LIMIT_CAPACITY = 100

fun Application.configureRateLimiting() {
    routing {
        route("/") {
            install(RateLimiting) {
                rateLimiter {
                    type = TokenBucket::class
                    capacity = RATE_LIMIT_CAPACITY
                    rate = 10.seconds
                }
            }
        }
    }
}
