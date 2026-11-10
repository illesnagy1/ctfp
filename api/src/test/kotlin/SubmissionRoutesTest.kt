package com.ctfp

import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.Limits
import com.ctfp.dto.SubmissionResult
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.eq
import kotlin.test.Test
import kotlin.test.assertEquals

class SubmissionRoutesTest {
    private suspend fun ApplicationTestBuilder.post(
        path: String,
        token: String?,
        body: String,
    ) = client.post(path) {
        token?.let { bearerAuth(it) }
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    /** A released challenge with one flag; returns its id. */
    private suspend fun ApplicationTestBuilder.challengeWithFlag(
        admin: String,
        title: String,
        flag: String,
        maxAttempts: Int = 0,
        isVisible: Boolean = true,
    ): Int {
        post(
            "/v1/challenges",
            admin,
            """{"title":"$title","description":null,"isVisible":$isVisible,""" +
                """"publishAt":"2026-01-01T00:00:00Z","maxAttempts":$maxAttempts,"categories":[],"timeLimit":0}""",
        )
        val all = client.get("/v1/challenges") { bearerAuth(admin) }.bodyAsText()
        val id = Json.decodeFromString<List<ChallengeResponse>>(all).single { it.title == title }.id
        assertEquals(HttpStatusCode.Created, post("/v1/challenges/$id/flags", admin, flag).status)
        return id
    }

    private suspend fun ApplicationTestBuilder.submit(
        challengeId: Int,
        token: String?,
        flag: String,
    ) = post("/v1/challenges/$challengeId/submissions", token, """{"flag":"$flag"}""")

    private suspend fun HttpResponse.result() = Json.decodeFromString<SubmissionResult>(bodyAsText())

    @Test
    fun `players solve challenges with the right flag`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("submit-admin")
            val player = register("submit-player")
            val id = challengeWithFlag(admin, "solvable", """{"pattern":"flag\\{right\\}","baseValue":100,"isCaseSensitive":false}""")

            assertEquals(HttpStatusCode.Unauthorized, submit(id, null, "flag{right}").status)
            assertEquals(SubmissionResult(correct = false), submit(id, player, "flag{wrong}").result())
            // Trimmed, and this flag ignores case.
            assertEquals(SubmissionResult(correct = true), submit(id, player, "  FLAG{Right} ").result())
            assertEquals(SubmissionResult(correct = true, alreadySolved = true), submit(id, player, "anything").result())
            assertEquals("[$id]", client.get("/v1/users/me/solves") { bearerAuth(player) }.bodyAsText())
            assertEquals("[]", client.get("/v1/users/me/solves") { bearerAuth(admin) }.bodyAsText())
        }

    @Test
    fun `wrong guesses are limited by max attempts`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("attempts-admin")
            val player = register("attempts-player")
            val id = challengeWithFlag(admin, "limited", """{"pattern":"secret","baseValue":10}""", maxAttempts = 2)

            assertEquals(SubmissionResult(correct = false, attemptsLeft = 1), submit(id, player, "nope").result())
            assertEquals(SubmissionResult(correct = false, attemptsLeft = 0), submit(id, player, "nope").result())
            val blocked = submit(id, player, "secret")
            assertEquals(HttpStatusCode.Forbidden, blocked.status)
            assertEquals("""{"message":"No attempts left"}""", blocked.bodyAsText())
        }

    @Test
    fun `hidden challenges can't be solved by players`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("hidden-submit-admin")
            val player = register("hidden-submit-player")
            val id = challengeWithFlag(admin, "hidden-solvable", """{"pattern":"x","baseValue":1}""", isVisible = false)

            assertEquals(HttpStatusCode.NotFound, submit(id, player, "x").status)
            // Admins can try their own hidden challenges.
            assertEquals(SubmissionResult(correct = true), submit(id, admin, "x").result())
        }

    @Test
    fun `blank, too long and banned submissions are rejected`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("reject-admin")
            val player = register("reject-player")
            val id = challengeWithFlag(admin, "rejecting", """{"pattern":"flag","baseValue":100,"isCaseSensitive":false}""")

            assertEquals(HttpStatusCode.BadRequest, submit(id, player, "   ").status)
            assertEquals(HttpStatusCode.BadRequest, submit(id, player, "x".repeat(Limits.SUBMITTED_FLAG_MAX + 1)).status)

            withTransaction { UserDAO.find { UserTable.username eq "reject-player" }.single().isBanned = true }
            assertEquals(HttpStatusCode.Forbidden, submit(id, player, "flag").status)
        }
}
