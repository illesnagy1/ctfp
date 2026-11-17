package com.ctfp

import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.config.mergeWith
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RateLimitTest {
    /** The real app, with small limits instead of the test task's huge ones. */
    private fun limited(
        auth: Int,
        submissions: Int,
        block: suspend ApplicationTestBuilder.() -> Unit,
    ) = testApplication {
        environment {
            config =
                ApplicationConfig("application.conf").mergeWith(
                    MapApplicationConfig(
                        "rateLimit.global" to "1000",
                        "rateLimit.auth" to "$auth",
                        "rateLimit.submissions" to "$submissions",
                    ),
                )
        }
        startApplication()
        block()
    }

    private suspend fun ApplicationTestBuilder.login(address: String) =
        client.request("/v1/auth/login") {
            method = HttpMethod.Post
            header(HttpHeaders.XForwardedFor, address)
            header(HttpHeaders.ContentType, "application/json")
            setBody("""{"login":"nobody","password":"wrong-password"}""")
        }

    @Test
    fun `logins are limited per address, with standard headers and a problem`() =
        limited(auth = 2, submissions = 1) {
            val first = login("203.0.113.1")
            assertEquals(HttpStatusCode.Unauthorized, first.status)
            assertEquals(
                listOf("\"global\";q=1000;w=10", "\"auth\";q=2;w=60"),
                first.headers
                    .getAll("RateLimit-Policy")
                    ?.sorted()
                    ?.reversed(),
            )
            assertTrue(
                first.headers
                    .getAll("RateLimit")
                    .orEmpty()
                    .any { it.startsWith("\"auth\";r=1;t=") },
            )

            login("203.0.113.1")
            val refused = login("203.0.113.1")
            assertEquals(HttpStatusCode.TooManyRequests, refused.status)
            val wait = refused.headers[HttpHeaders.RetryAfter]?.toLong()
            assertTrue(wait != null && wait in 1..60, "Retry-After: $wait")
            assertEquals("Too many requests, try again in $wait seconds", refused.problem().detail)

            // Another client has its own bucket.
            assertEquals(HttpStatusCode.Unauthorized, login("203.0.113.2").status)
        }

    @Test
    fun `flag submissions are limited per player`() =
        limited(auth = 100, submissions = 1) {
            val admin = registerAdmin("limit-admin")
            val first = register("limit-first")
            val second = register("limit-second")
            val id = challengeWithFlag(admin, "limited-submissions", """{"pattern":"limited-flag","baseValue":1}""")

            suspend fun submit(token: String) = call(HttpMethod.Post, "/v1/challenges/$id/submissions", token, """{"flag":"nope"}""").status

            assertEquals(HttpStatusCode.OK, submit(first))
            assertEquals(HttpStatusCode.TooManyRequests, submit(first))
            assertEquals(HttpStatusCode.OK, submit(second))
        }
}
