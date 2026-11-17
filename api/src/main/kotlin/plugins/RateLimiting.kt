package com.ctfp.plugins

import com.ctfp.config.rateLimitConfig
import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.principal
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.RateLimitProviderConfig
import io.ktor.server.plugins.ratelimit.RateLimiter
import io.ktor.server.response.header
import kotlin.math.ceil
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/** Logins, registrations and password resets: guessing passwords or flooding mail. Wrap routes with `rateLimit`. */
val AuthRateLimit = RateLimitName("auth")

/** Flag submissions, per player: guessing flags. Must be inside `authenticated`, which knows the player. */
val SubmissionRateLimit = RateLimitName("submissions")

/**
 * Token buckets per client IP (from X-Forwarded-For, which the frontend sets) or per player. Every response says where
 * it stands with the IETF `RateLimit-Policy` and `RateLimit` headers (draft-ietf-httpapi-ratelimit-headers); a refused
 * one is a 429 with `Retry-After` (RFC 9110), as problem details (StatusPages.kt).
 */
fun Application.configureRateLimiting() {
    val limits = environment.config.rateLimitConfig()
    install(RateLimit) {
        global { limit("global", limits.global, 10.seconds) { clientAddress } }
        register(AuthRateLimit) { limit("auth", limits.auth, 1.minutes) { clientAddress } }
        register(SubmissionRateLimit) {
            limit("submissions", limits.submissions, 1.minutes) { principal<UserPrincipal>()?.id ?: clientAddress }
        }
    }
}

private val ApplicationCall.clientAddress: String get() = request.origin.remoteAddress

private fun RateLimitProviderConfig.limit(
    policy: String,
    quota: Int,
    window: Duration,
    key: ApplicationCall.() -> Any,
) {
    rateLimiter(limit = quota, refillPeriod = window)
    requestKey { call -> call.key() }
    modifyResponse { call, state -> call.rateLimitHeaders(policy, quota, window, state) }
}

/** Appended, not set: a route can be under several limits (the global one and its own). */
private fun ApplicationCall.rateLimitHeaders(
    policy: String,
    quota: Int,
    window: Duration,
    state: RateLimiter.State,
) {
    response.headers.append("RateLimit-Policy", "\"$policy\";q=$quota;w=${window.inWholeSeconds}")
    when (state) {
        is RateLimiter.State.Available -> {
            val reset = (state.refillAtTimeMillis - System.currentTimeMillis()).coerceAtLeast(0)
            response.headers.append("RateLimit", "\"$policy\";r=${state.remainingTokens};t=${reset.ceilSeconds()}")
        }

        is RateLimiter.State.Exhausted -> {
            val wait = state.toWait.inWholeMilliseconds.ceilSeconds()
            response.headers.append("RateLimit", "\"$policy\";r=0;t=$wait")
            response.header(HttpHeaders.RetryAfter, wait)
        }
    }
}

private fun Long.ceilSeconds() = ceil(this / 1000.0).toLong()
