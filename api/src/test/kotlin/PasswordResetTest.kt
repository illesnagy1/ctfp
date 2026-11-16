package com.ctfp

import com.ctfp.service.PasswordResetService
import com.ctfp.service.helper.MailSender
import com.ctfp.service.helper.PasswordHasher
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.coroutineScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The embedded database is shared between tests, so each one uses its own users. */
class PasswordResetTest {
    private suspend fun ApplicationTestBuilder.reset(
        token: String,
        password: String = "new-battery-staple",
    ) = call(HttpMethod.Post, "/v1/auth/reset-password", body = """{"token":"$token","password":"$password"}""")

    private suspend fun ApplicationTestBuilder.login(password: String) =
        call(HttpMethod.Post, "/v1/auth/login", body = """{"login":"reset-user","password":"$password"}""")

    /** Requests a reset through a service whose mail is captured, and returns the token from the link, if one was sent. */
    private suspend fun requestToken(email: String): String? =
        coroutineScope {
            var text: String? = null
            val service = PasswordResetService(PasswordHasher(), MailSender { _, _, body -> text = body }, "https://ctf.test/", this)
            service.request(email)?.join()
            text?.let { Regex("""https://ctf\.test/reset-password\?token=(\S+)""").find(it)?.groupValues?.get(1) }
        }

    @Test
    fun `a reset link sets a new password once`() =
        testApplication {
            configure()
            startApplication()
            register("reset-user")

            // Unknown addresses look the same to the caller and send nothing.
            assertEquals(
                HttpStatusCode.Accepted,
                call(HttpMethod.Post, "/v1/auth/forgot-password", body = """{"email":"nobody@example.com"}""").status,
            )
            assertNull(requestToken("nobody@example.com"))

            val token = assertNotNull(requestToken(" Reset-User@Example.com "))
            // One email per cooldown.
            assertNull(requestToken("reset-user@example.com"))

            assertEquals(HttpStatusCode.BadRequest, reset(token, password = "short").status)
            assertEquals(HttpStatusCode.NoContent, reset(token).status)
            assertEquals(HttpStatusCode.OK, login("new-battery-staple").status)
            assertEquals(HttpStatusCode.Unauthorized, login("correct-horse").status)

            val reused = reset(token, password = "third-password")
            assertEquals(HttpStatusCode.BadRequest, reused.status)
            assertTrue("invalid or has expired" in reused.bodyAsText())
        }
}
