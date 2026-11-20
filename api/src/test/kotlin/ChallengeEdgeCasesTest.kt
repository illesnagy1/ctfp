package com.ctfp

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.FlagResponse
import com.ctfp.dto.SubmissionResult
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/** Challenge and flag routes beyond the happy paths. The embedded database is shared, so names are unique per test. */
class ChallengeEdgeCasesTest {
    private fun challenge(title: String) =
        """{"title":"$title","description":"# md","publishAt":"2026-01-01T00:00:00Z","maxAttempts":2,"categories":["web"],"timeLimit":60}"""

    @Test
    fun `admins read and update single challenges`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("edge-challenge-admin")
            assertEquals(HttpStatusCode.Created, call(HttpMethod.Post, "/v1/challenges", admin, challenge("edge-one")).status)
            val id = challengeId("edge-one")

            assertEquals(HttpStatusCode.NoContent, call(HttpMethod.Put, "/v1/challenges/$id", admin, challenge("edge-one-renamed")).status)
            val read = call(HttpMethod.Get, "/v1/challenges/$id")
            assertEquals(HttpStatusCode.OK, read.status)
            val response = Json.decodeFromString<ChallengeResponse>(read.bodyAsText())
            assertEquals("edge-one-renamed", response.title)
            assertEquals(listOf("web"), response.categories)

            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Get, "/v1/challenges/${Int.MAX_VALUE}", admin).status)
            assertEquals(
                HttpStatusCode.NotFound,
                call(HttpMethod.Put, "/v1/challenges/${Int.MAX_VALUE}", admin, challenge("edge-ghost")).status,
            )
            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Delete, "/v1/challenges/${Int.MAX_VALUE}", admin).status)
            assertEquals(HttpStatusCode.BadRequest, call(HttpMethod.Get, "/v1/challenges/abc").status)
            assertEquals(HttpStatusCode.BadRequest, call(HttpMethod.Post, "/v1/challenges", admin, """{"title":1}""").status)
        }

    @Test
    fun `admins delete single and many challenges`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("edge-delete-admin")
            val ids =
                listOf("edge-del-a", "edge-del-b", "edge-del-c").map {
                    call(HttpMethod.Post, "/v1/challenges", admin, challenge(it))
                    challengeId(it)
                }

            assertEquals(HttpStatusCode.NoContent, call(HttpMethod.Delete, "/v1/challenges/${ids[0]}", admin).status)
            assertEquals(HttpStatusCode.NoContent, call(HttpMethod.Delete, "/v1/challenges", admin, "[${ids[1]},${ids[2]}]").status)
            assertTrue(withTransaction { ChallengeDAO.forIds(ids).empty() })
        }

    @Test
    fun `admins update and delete flags`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("edge-flag-admin")
            val challenge = challengeWithFlag(admin, "edge-flags", """{"pattern":"edge-flag","baseValue":100}""")
            val flags =
                Json.decodeFromString<List<FlagResponse>>(
                    call(HttpMethod.Get, "/v1/challenges/$challenge/flags", admin).bodyAsText(),
                )
            val flagId = flags.single().id

            val update = """{"pattern":"edge-flag-2","baseValue":50,"decayFun":"logarithmic","decayValue":5,"minValue":10}"""
            assertEquals(HttpStatusCode.NoContent, call(HttpMethod.Put, "/v1/flags/$flagId", admin, update).status)
            val updated =
                Json.decodeFromString<List<FlagResponse>>(
                    call(HttpMethod.Get, "/v1/challenges/$challenge/flags", admin).bodyAsText(),
                )
            assertEquals("edge-flag-2", updated.single().pattern)

            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Put, "/v1/flags/${Int.MAX_VALUE}", admin, update).status)
            assertEquals(
                HttpStatusCode.BadRequest,
                call(HttpMethod.Put, "/v1/flags/$flagId", admin, """{"pattern":"","baseValue":1}""").status,
            )
            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Get, "/v1/challenges/${Int.MAX_VALUE}/flags", admin).status)

            assertEquals(HttpStatusCode.NoContent, call(HttpMethod.Delete, "/v1/flags/$flagId", admin).status)
            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Delete, "/v1/flags/$flagId", admin).status)
        }

    @Test
    fun `flag validation covers every rule`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("edge-rules-admin")
            val challenge = challengeWithFlag(admin, "edge-rules", """{"pattern":"edge-rules","baseValue":1}""")
            val invalid =
                mapOf(
                    "blank" to """{"pattern":"  ","baseValue":1}""",
                    "too long" to """{"pattern":"${"a".repeat(10_000)}","baseValue":1}""",
                    "negative value" to """{"pattern":"edge-neg","baseValue":-1}""",
                    "no minimum" to """{"pattern":"edge-min","baseValue":10,"decayFun":"linear","decayValue":1}""",
                    "negative decay" to """{"pattern":"edge-dec","baseValue":10,"decayFun":"linear","decayValue":-1,"minValue":0}""",
                    "negative minimum" to """{"pattern":"edge-lo","baseValue":10,"decayFun":"parabolic","decayValue":1,"minValue":-1}""",
                    "minimum over value" to """{"pattern":"edge-hi","baseValue":10,"decayFun":"parabolic","decayValue":1,"minValue":11}""",
                )
            for ((case, body) in invalid) {
                assertEquals(HttpStatusCode.BadRequest, call(HttpMethod.Post, "/v1/challenges/$challenge/flags", admin, body).status, case)
            }
        }

    @Test
    fun `a catastrophically backtracking flag pattern gives up instead of hanging`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("edge-redos-admin")
            val player = register("edge-redos-player")
            val id = challengeWithFlag(admin, "edge-redos", """{"pattern":"(a+)+b","baseValue":1}""")

            val started = TimeSource.Monotonic.markNow()
            val response = call(HttpMethod.Post, "/v1/challenges/$id/submissions", player, """{"flag":"${"a".repeat(64)}"}""")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(SubmissionResult(correct = false), Json.decodeFromString<SubmissionResult>(response.bodyAsText()))
            assertTrue(started.elapsedNow() < 5.seconds)
        }
}
