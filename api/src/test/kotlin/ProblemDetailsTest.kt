package com.ctfp

import com.ctfp.dto.FieldProblem
import com.ctfp.dto.ProblemDetails
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

/** Every error is RFC 9457 problem details, sent as application/problem+json. */
class ProblemDetailsTest {
    @Test
    fun `service errors keep their detail`() =
        testApplication {
            configure()
            val token = register("problem-viewer")
            val response = client.get("/v1/users/999999") { bearerAuth(token) }
            assertEquals(
                ProblemDetails(title = "Not Found", status = 404, detail = "User 999999 not found", instance = "/v1/users/999999"),
                response.problem(),
            )
        }

    @Test
    fun `auth, unknown routes and validation answer with problems too`() =
        testApplication {
            configure()
            val player = register("problem-player")

            assertEquals("Authentication required", client.get("/v1/users").problem().detail)
            assertEquals("Admin access required", client.get("/v1/admin/users") { bearerAuth(player) }.problem().detail)

            val unknown = client.get("/v1/no-such-route")
            assertEquals(HttpStatusCode.NotFound, unknown.status)
            assertEquals(ProblemDetails(title = "Not Found", status = 404, instance = "/v1/no-such-route"), unknown.problem())

            val invalid = call(HttpMethod.Post, "/v1/auth/register", body = """{"username":"x","email":"nope","password":"short"}""")
            assertEquals(
                listOf(
                    FieldProblem("#/username", "username must match ^[A-Za-z0-9._-]{3,64}$"),
                    FieldProblem("#/username", "username must be at least 3 characters"),
                    FieldProblem("#/email", "email must be an email address"),
                    FieldProblem("#/password", "password must be at least 8 characters"),
                ),
                invalid.problem().errors,
            )
        }
}
