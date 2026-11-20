package com.ctfp

import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeStartTable
import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.ScoreboardEntry
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

/** CTF start, end and scoreboard freeze (settings), and timed challenges. */
class CtfWindowTest {
    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun ApplicationTestBuilder.withSettings(
        admin: String,
        settings: String,
        block: suspend () -> Unit,
    ) {
        assertEquals(HttpStatusCode.OK, call(HttpMethod.Put, "/v1/admin/settings", admin, settings).status)
        try {
            block()
        } finally {
            call(HttpMethod.Put, "/v1/admin/settings", admin, """{"ctfName":"CTFP"}""")
        }
    }

    private suspend fun ApplicationTestBuilder.submit(
        id: Int,
        token: String,
        flag: String,
    ) = call(HttpMethod.Post, "/v1/challenges/$id/submissions", token, """{"flag":"$flag"}""")

    private suspend fun ApplicationTestBuilder.challenge(
        id: Int,
        token: String,
    ) = json.decodeFromString<ChallengeResponse>(call(HttpMethod.Get, "/v1/challenges/$id", token).bodyAsText())

    @Test
    fun `timed challenges unlock on start and close when time is up`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("timed-admin")
            val player = register("timed-player")
            val id = challengeWithFlag(admin, "timed-challenge", """{"pattern":"timed-flag","baseValue":1}""")
            val body =
                """{"title":"timed-challenge","description":"secret","publishAt":"2026-01-01T00:00:00Z","maxAttempts":0,""" +
                    """"categories":[],"timeLimit":3600}"""
            assertEquals(HttpStatusCode.NoContent, call(HttpMethod.Put, "/v1/challenges/$id", admin, body).status)

            assertNull(challenge(id, player).description)
            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Get, "/v1/challenges/$id/files", player).status)
            assertEquals(HttpStatusCode.Forbidden, submit(id, player, "timed-flag").status)

            assertEquals(HttpStatusCode.OK, call(HttpMethod.Post, "/v1/challenges/$id/start", player).status)
            val started = challenge(id, player)
            assertEquals("secret", started.description)
            assertNotNull(started.startedAt)
            assertEquals(HttpStatusCode.OK, call(HttpMethod.Get, "/v1/challenges/$id/files", player).status)
            // A second start keeps the first.
            call(HttpMethod.Post, "/v1/challenges/$id/start", player)
            assertEquals(started.startedAt, challenge(id, player).startedAt)

            withTransaction {
                ChallengeStartTable.update({ ChallengeStartTable.challengeId eq id }) { it[startedAt] = Clock.System.now() - 2.hours }
            }
            val late = submit(id, player, "timed-flag")
            assertEquals(HttpStatusCode.Forbidden, late.status)
            assertEquals("Time is up for this challenge", late.problem().detail)
        }

    @Test
    fun `challenges wait for the start and flags stop at the end`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("window-admin")
            val player = register("window-player")
            val id = challengeWithFlag(admin, "window-challenge", """{"pattern":"window-flag","baseValue":1}""")
            val later = Clock.System.now() + 1.hours
            val earlier = Clock.System.now() - 1.hours

            withSettings(admin, """{"startsAt":"$later"}""") {
                assertEquals("[]", call(HttpMethod.Get, "/v1/challenges", player).bodyAsText())
                assertEquals(HttpStatusCode.NotFound, submit(id, player, "window-flag").status)
                assertEquals(HttpStatusCode.OK, call(HttpMethod.Get, "/v1/challenges/$id", admin).status)
            }
            withSettings(admin, """{"endsAt":"$earlier"}""") {
                val closed = submit(id, player, "window-flag")
                assertEquals(HttpStatusCode.Forbidden, closed.status)
                assertEquals("The CTF is over", closed.problem().detail)
            }
        }

    @Test
    fun `the public scoreboard freezes, admins see it live`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("freeze-admin")
            val player = register("freeze-player")
            val id = challengeWithFlag(admin, "freeze-challenge", """{"pattern":"freeze-flag","baseValue":100}""")

            fun List<ScoreboardEntry>.points() = single { it.username == "freeze-player" }.points

            withSettings(admin, """{"freezeAt":"${Clock.System.now() - 1.hours}"}""") {
                assertEquals(HttpStatusCode.OK, submit(id, player, "freeze-flag").status)
                val public = json.decodeFromString<List<ScoreboardEntry>>(call(HttpMethod.Get, "/v1/scoreboard").bodyAsText())
                val live = json.decodeFromString<List<ScoreboardEntry>>(call(HttpMethod.Get, "/v1/scoreboard", admin).bodyAsText())
                assertEquals(0, public.points())
                assertTrue(live.points() > 0)
                assertEquals(0, challenge(id, player).solves)
            }
        }
}
