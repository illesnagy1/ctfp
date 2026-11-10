package com.ctfp

import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.ScoreboardEntry
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScoreboardRoutesTest {
    private suspend fun ApplicationTestBuilder.post(
        path: String,
        token: String,
        body: String,
    ) = client.post(path) {
        bearerAuth(token)
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    private suspend fun ApplicationTestBuilder.challengeWithFlag(
        admin: String,
        title: String,
        flag: String,
    ): Int {
        post(
            "/v1/challenges",
            admin,
            """{"title":"$title","description":null,"publishAt":"2026-01-01T00:00:00Z","maxAttempts":0,"categories":[],"timeLimit":0}""",
        )
        val all = client.get("/v1/challenges") { bearerAuth(admin) }.bodyAsText()
        val id = Json.decodeFromString<List<ChallengeResponse>>(all).single { it.title == title }.id
        assertEquals(HttpStatusCode.Created, post("/v1/challenges/$id/flags", admin, flag).status)
        return id
    }

    private suspend fun ApplicationTestBuilder.solve(
        challengeId: Int,
        token: String,
        flag: String,
    ) = assertEquals(HttpStatusCode.OK, post("/v1/challenges/$challengeId/submissions", token, """{"flag":"$flag"}""").status)

    @Test
    fun `players are ranked by points with decaying flag values`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("score-admin")
            val first = register("score-first")
            val second = register("score-second")
            register("score-none")
            val static = challengeWithFlag(admin, "score-static", """{"pattern":"a","baseValue":100}""")
            val linear =
                challengeWithFlag(
                    admin,
                    "score-linear",
                    """{"pattern":"b","baseValue":200,"decayFun":"linear","decayValue":50,"minValue":100}""",
                )

            solve(static, first, "a")
            solve(linear, first, "b")
            solve(linear, second, "b")
            // Admins aren't ranked and don't lower the value.
            solve(linear, admin, "b")

            // Public, no login needed.
            val response = client.get("/v1/scoreboard")
            assertEquals(HttpStatusCode.OK, response.status)
            val board = Json.decodeFromString<List<ScoreboardEntry>>(response.bodyAsText())
            val mine = board.filter { it.username.startsWith("score-") }

            // Two players solved the linear flag: 200 - 50 for both of them.
            assertEquals(
                listOf("score-first" to 250, "score-second" to 150, "score-none" to 0),
                mine.map { it.username to it.points },
            )
            assertEquals(listOf(2, 1, 0), mine.map { it.solves })
            assertNull(mine.last().lastSolveAt)
            assertEquals(board.sortedByDescending { it.points }.map { it.points }, board.map { it.points })
        }
}
