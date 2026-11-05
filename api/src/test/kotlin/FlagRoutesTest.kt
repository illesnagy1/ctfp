package com.ctfp

import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.DecayFunction
import com.ctfp.dto.FlagResponse
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
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
import kotlin.test.assertTrue

class FlagRoutesTest {
    private suspend fun ApplicationTestBuilder.createChallenge(admin: String, title: String): Int {
        client.post("/v1/challenges") {
            bearerAuth(admin)
            contentType(ContentType.Application.Json)
            setBody("""{"title":"$title","description":null,"publishAt":"2026-01-01T00:00:00Z","maxAttempts":0,"categories":[],"timeLimit":0}""")
        }
        val all = client.get("/v1/challenges") { bearerAuth(admin) }.bodyAsText()
        return Json.decodeFromString<List<ChallengeResponse>>(all).single { it.title == title }.id
    }

    private suspend fun ApplicationTestBuilder.addFlag(challengeId: Int, token: String?, body: String) =
        client.post("/v1/challenges/$challengeId/flags") {
            token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            setBody(body)
        }

    @Test
    fun `only admins can see and manage flags`() = testApplication {
        configure()
        startApplication()
        val admin = registerAdmin("flag-admin")
        val player = register("flag-player")
        val challengeId = createChallenge(admin, "flagged")
        val body = """{"pattern":"flag\\{only-admins\\}","baseValue":100}"""

        for ((token, status) in listOf(null to HttpStatusCode.Unauthorized, player to HttpStatusCode.Forbidden)) {
            assertEquals(status, client.get("/v1/challenges/$challengeId/flags") { token?.let { bearerAuth(it) } }.status)
            assertEquals(status, addFlag(challengeId, token, body).status)
        }

        val created = addFlag(challengeId, admin, body)
        assertEquals(HttpStatusCode.Created, created.status)
        val flag = Json.decodeFromString<FlagResponse>(created.bodyAsText())
        assertEquals("flag\\{only-admins\\}", flag.pattern)
        assertEquals(DecayFunction.STATIC, flag.decayFun)
        assertTrue(flag.isCaseSensitive)

        val listed = client.get("/v1/challenges/$challengeId/flags") { bearerAuth(admin) }.bodyAsText()
        assertEquals(listOf(flag.id), Json.decodeFromString<List<FlagResponse>>(listed).map { it.id })

        assertEquals(HttpStatusCode.Forbidden, client.delete("/v1/flags/${flag.id}") { bearerAuth(player) }.status)
        assertEquals(HttpStatusCode.NoContent, client.delete("/v1/flags/${flag.id}") { bearerAuth(admin) }.status)
        assertEquals("[]", client.get("/v1/challenges/$challengeId/flags") { bearerAuth(admin) }.bodyAsText())
    }

    @Test
    fun `invalid flags are rejected`() = testApplication {
        configure()
        startApplication()
        val admin = registerAdmin("flag-validation-admin")
        val challengeId = createChallenge(admin, "flag-validation")

        val badRegex = addFlag(challengeId, admin, """{"pattern":"flag{(","baseValue":100}""")
        assertEquals(HttpStatusCode.BadRequest, badRegex.status)
        assertTrue("regular expression" in badRegex.bodyAsText())

        val noDecay = addFlag(challengeId, admin, """{"pattern":"x","baseValue":100,"decayFun":"linear"}""")
        assertEquals(HttpStatusCode.BadRequest, noDecay.status)

        assertEquals(HttpStatusCode.NotFound, addFlag(999_999, admin, """{"pattern":"x","baseValue":1}""").status)
    }
}
