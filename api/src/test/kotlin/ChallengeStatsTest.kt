package com.ctfp

import com.ctfp.dto.ChallengeResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The embedded database is shared between tests, so each one uses its own users and challenge titles. */
class ChallengeStatsTest {
    private suspend fun ApplicationTestBuilder.challenge(
        id: Int,
        token: String?,
    ) = Json.decodeFromString<ChallengeResponse>(call(HttpMethod.Get, "/v1/challenges/$id", token).bodyAsText())

    private suspend fun ApplicationTestBuilder.submit(
        id: Int,
        token: String,
        flag: String,
    ) = call(HttpMethod.Post, "/v1/challenges/$id/submissions", token, """{"flag":"$flag"}""")

    @Test
    fun `value, solves and the caller's progress follow the submissions`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("stats-admin")
            val first = register("stats-first")
            val second = register("stats-second")
            val id =
                challengeWithFlag(
                    admin,
                    "stats-challenge",
                    """{"pattern":"stats-flag","baseValue":100,"decayFun":"linear","decayValue":10,"minValue":50}""",
                    maxAttempts = 3,
                )

            val fresh = challenge(id, first)
            assertEquals(100, fresh.value)
            assertEquals(0, fresh.solves)
            assertEquals(false, fresh.solved)
            assertEquals(3, fresh.attemptsLeft)

            submit(id, first, "wrong")
            assertEquals(2, challenge(id, first).attemptsLeft)

            submit(id, first, "stats-flag")
            submit(id, second, "stats-flag")
            val solved = challenge(id, first)
            assertEquals(true, solved.solved)
            assertNull(solved.attemptsLeft)
            // Two solvers: one linear decay step.
            assertEquals(90, solved.value)
            assertEquals(2, solved.solves)

            // Admins solving don't count, and guests get no progress.
            submit(id, admin, "stats-flag")
            val guest = challenge(id, null)
            assertEquals(2, guest.solves)
            assertNull(guest.solved)
            assertNull(guest.attemptsLeft)
        }
}
