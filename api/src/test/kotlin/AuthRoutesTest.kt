package com.ctfp

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ctfp.domain.dao.RegistrationCodeDAO
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.AuthResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/** The embedded database is shared between tests, so each one uses its own names. */
class AuthRoutesTest {
    private suspend fun ApplicationTestBuilder.login(
        login: String,
        password: String = "correct-horse",
    ) = call(HttpMethod.Post, "/v1/auth/login", body = """{"login":"$login","password":"$password"}""")

    private suspend fun ApplicationTestBuilder.registerWith(
        name: String,
        email: String = "$name@example.com",
        password: String = "correct-horse",
        code: String? = null,
    ) = call(
        HttpMethod.Post,
        "/v1/auth/register",
        body = """{"username":"$name","email":"$email","password":"$password","registrationCode":${code?.let { "\"$it\"" }}}""",
    )

    private suspend fun ApplicationTestBuilder.me(token: String?) = call(HttpMethod.Get, "/v1/auth/me", token)

    private suspend fun registrationCode(
        code: String,
        maxUses: Int,
        expiresIn: kotlin.time.Duration = 1.days,
    ) = withTransaction {
        RegistrationCodeDAO.new {
            this.code = code
            this.maxUses = maxUses
            expiresAt = Clock.System.now() + expiresIn
        }
    }

    @Test
    fun `users log in with their username or email`() =
        testApplication {
            configure()
            startApplication()
            register("login-user")

            for (login in listOf("login-user", "LOGIN-user@Example.com", "  login-user  ")) {
                val response = login(login)
                assertEquals(HttpStatusCode.OK, response.status, login)
                val auth = Json.decodeFromString<AuthResponse>(response.bodyAsText())
                assertEquals("login-user", auth.user.username)
                assertEquals(HttpStatusCode.OK, me(auth.token).status)
            }
        }

    @Test
    fun `wrong passwords, unknown users and banned users can't log in`() =
        testApplication {
            configure()
            startApplication()
            register("login-banned")
            register("login-wrong")
            withTransaction { UserDAO[userId("login-banned")].isBanned = true }

            val wrong = login("login-wrong", "not-the-password")
            val unknown = login("login-nobody")
            // Same answer for both, so it doesn't reveal which usernames exist.
            assertEquals(HttpStatusCode.Unauthorized, wrong.status)
            assertEquals(HttpStatusCode.Unauthorized, unknown.status)
            assertEquals(wrong.bodyAsText(), unknown.bodyAsText())
            // Twice, so the cached dummy hash is used as well.
            assertEquals(HttpStatusCode.Unauthorized, login("login-nobody-2").status)

            assertEquals(HttpStatusCode.Forbidden, login("login-banned").status)
            assertEquals(HttpStatusCode.Unauthorized, login("login-banned", "not-the-password").status)
        }

    @Test
    fun `me needs a token of a user that still exists and isn't banned`() =
        testApplication {
            configure()
            startApplication()
            val banned = register("me-banned")
            val deleted = register("me-deleted")

            assertEquals(HttpStatusCode.Unauthorized, me(null).status)
            assertEquals(HttpStatusCode.Unauthorized, me("not-a-jwt").status)
            assertEquals(HttpStatusCode.OK, me(banned).status)

            withTransaction {
                UserDAO[userId("me-banned")].isBanned = true
                UserDAO[userId("me-deleted")].delete()
            }
            assertEquals(HttpStatusCode.Unauthorized, me(banned).status)
            assertEquals(HttpStatusCode.Unauthorized, me(deleted).status)
        }

    @Test
    fun `tokens without a numeric subject are rejected`() =
        testApplication {
            configure()
            startApplication()
            // Signed with the dev secret from application.conf, so only the subject is wrong.
            val token =
                JWT
                    .create()
                    .withIssuer("ctfp-api")
                    .withAudience("ctfp-frontend")
                    .withSubject("not-a-number")
                    .sign(Algorithm.HMAC256("dev-only-insecure-jwt-secret-change-me"))

            assertEquals(HttpStatusCode.Unauthorized, me(token).status)
        }

    @Test
    fun `registration validates its input`() =
        testApplication {
            configure()
            startApplication()

            assertEquals(HttpStatusCode.BadRequest, registerWith("x").status)
            assertEquals(HttpStatusCode.BadRequest, registerWith("bad name!").status)
            assertEquals(HttpStatusCode.BadRequest, registerWith("reg-email", email = "not-an-email").status)
            assertEquals(HttpStatusCode.BadRequest, registerWith("reg-email-long", email = "a".repeat(300) + "@example.com").status)
            assertEquals(HttpStatusCode.BadRequest, registerWith("reg-short", password = "short").status)
            assertEquals(HttpStatusCode.BadRequest, registerWith("reg-long", password = "p".repeat(1000)).status)
            assertEquals(HttpStatusCode.BadRequest, call(HttpMethod.Post, "/v1/auth/register", body = "{not json").status)

            assertEquals(HttpStatusCode.Created, registerWith("reg-taken").status)
            assertEquals(HttpStatusCode.Conflict, registerWith("reg-taken", email = "other@example.com").status)
            assertEquals(HttpStatusCode.Conflict, registerWith("reg-other", email = "REG-taken@example.com").status)
        }

    @Test
    fun `registration codes are checked for existence, expiry and uses`() =
        testApplication {
            configure()
            startApplication()
            registrationCode("code-once", maxUses = 1)
            registrationCode("code-expired", maxUses = 10, expiresIn = (-1).days)

            assertEquals(HttpStatusCode.BadRequest, registerWith("code-unknown", code = "no-such-code").status)
            assertEquals(HttpStatusCode.BadRequest, registerWith("code-late", code = "code-expired").status)
            assertEquals(HttpStatusCode.Created, registerWith("code-first", code = " code-once ").status)
            assertEquals(HttpStatusCode.BadRequest, registerWith("code-second", code = "code-once").status)
            // Blank means no code.
            assertEquals(HttpStatusCode.Created, registerWith("code-blank", code = " ").status)

            val codeId = withTransaction { UserDAO[userId("code-first")].registrationCode?.id?.value }
            assertEquals(
                withTransaction {
                    RegistrationCodeDAO
                        .all()
                        .single { it.code == "code-once" }
                        .id.value
                },
                codeId,
            )
        }
}
