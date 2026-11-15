package com.ctfp

import com.ctfp.domain.dao.FlagDAO
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.FlagTable
import com.ctfp.dto.ScoreboardEntry
import com.ctfp.dto.SubmissionResult
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

/** Decay functions, scoreboard filtering and submission corner cases. The embedded database is shared, so names are unique. */
class ScoringEdgeCasesTest {
    private suspend fun ApplicationTestBuilder.submit(
        challengeId: Int,
        token: String,
        flag: String,
    ) = call(HttpMethod.Post, "/v1/challenges/$challengeId/submissions", token, """{"flag":"$flag"}""")

    private suspend fun ApplicationTestBuilder.board(prefix: String) =
        Json
            .decodeFromString<List<ScoreboardEntry>>(call(HttpMethod.Get, "/v1/scoreboard").bodyAsText())
            .filter { it.username.startsWith(prefix) }

    @Test
    fun `logarithmic and parabolic flags decay, and only ranked players count`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("decay-admin")
            val log =
                challengeWithFlag(
                    admin,
                    "decay-log",
                    """{"pattern":"decay-log","baseValue":100,"decayFun":"logarithmic","decayValue":10,"minValue":0}""",
                )
            val parabolic =
                challengeWithFlag(
                    admin,
                    "decay-par",
                    """{"pattern":"decay-par","baseValue":100,"decayFun":"parabolic","decayValue":10,"minValue":70}""",
                )
            val players = listOf("decay-a", "decay-b", "decay-c").map { register(it) }
            val hidden = register("decay-hidden")
            val banned = register("decay-banned")

            for (token in players + hidden + banned) {
                assertEquals(SubmissionResult(correct = true), Json.decodeFromString(submit(log, token, "decay-log").bodyAsText()))
                submit(parabolic, token, "decay-par")
            }
            withTransaction {
                UserDAO[userId("decay-hidden")].isHidden = true
                UserDAO[userId("decay-banned")].isBanned = true
            }

            val board = board("decay-")
            // Hidden and banned players are neither ranked nor counted as solvers: 3 solvers, so 2 after the first.
            assertEquals(listOf("decay-a", "decay-b", "decay-c"), board.map { it.username })
            // 100 - 10 * ln(3) = 89; 100 - 10 * 2² = 60, raised to the minimum of 70.
            assertEquals(listOf(159, 159, 159), board.map { it.points })
            assertEquals(listOf(2, 2, 2), board.map { it.solves })
        }

    @Test
    fun `solves of a deleted flag still count, without points`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("deleted-flag-admin")
            val id = challengeWithFlag(admin, "deleted-flag", """{"pattern":"deleted-flag","baseValue":100}""")
            val player = register("deleted-flag-player")
            submit(id, player, "deleted-flag")

            withTransaction { FlagDAO.find { FlagTable.challengeId eq id }.single().delete() }

            val entry = board("deleted-flag-player").single()
            assertEquals(1, entry.solves)
            assertEquals(0, entry.points)
        }

    @Test
    fun `case-sensitive flags, broken patterns and deleted submitters`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("submit-edge-admin")
            val sensitive =
                challengeWithFlag(admin, "submit-edge-case", """{"pattern":"Submit-Edge","baseValue":1,"isCaseSensitive":true}""")
            val broken = challengeWithFlag(admin, "submit-edge-broken", """{"pattern":"submit-edge-broken","baseValue":1}""")
            // Patterns are validated when saved, so a broken one can only come from the database.
            withTransaction { FlagTable.update({ FlagTable.challengeId eq broken }) { it[pattern] = "(" } }
            val player = register("submit-edge-player")
            val deleted = register("submit-edge-deleted")

            assertEquals(SubmissionResult(correct = false), Json.decodeFromString(submit(sensitive, player, "submit-edge").bodyAsText()))
            assertEquals(SubmissionResult(correct = true), Json.decodeFromString(submit(sensitive, player, "Submit-Edge").bodyAsText()))
            assertEquals(SubmissionResult(correct = false), Json.decodeFromString(submit(broken, player, "(").bodyAsText()))
            assertEquals(HttpStatusCode.NotFound, submit(Int.MAX_VALUE, player, "x").status)

            withTransaction { UserDAO[userId("submit-edge-deleted")].delete() }
            assertEquals(HttpStatusCode.Unauthorized, submit(sensitive, deleted, "Submit-Edge").status)
        }
}
