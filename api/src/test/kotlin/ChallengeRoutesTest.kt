package com.ctfp

import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.FlagTable
import com.ctfp.domain.model.HintTable
import com.ctfp.domain.model.HintUnlockTable
import com.ctfp.domain.model.SubmissionTable
import com.ctfp.domain.model.TeamTable
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.ChallengeResponse
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The embedded database is shared between tests, so each one uses its own users and challenge titles. */
class ChallengeRoutesTest {
    private fun challenge(title: String, isVisible: Boolean = true, publishAt: String = "2026-01-01T00:00:00Z") = """
        {"title":"$title","description":null,"isVisible":$isVisible,"publishAt":"$publishAt",
         "maxAttempts":0,"categories":["web"],"timeLimit":0}
    """.trimIndent()

    private suspend fun ApplicationTestBuilder.create(body: String, token: String?) = client.post("/v1/challenges") {
        token?.let { bearerAuth(it) }
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    private suspend fun ApplicationTestBuilder.challenges(token: String? = null): List<ChallengeResponse> {
        val response = client.get("/v1/challenges") { token?.let { bearerAuth(it) } }
        assertEquals(HttpStatusCode.OK, response.status)
        return Json.decodeFromString(response.bodyAsText())
    }

    @Test
    fun `creating a challenge with a taken title is a conflict`() = testApplication {
        configure()
        startApplication()
        val admin = registerAdmin("conflict-admin")
        val body = challenge("duplicate-title")

        assertEquals(HttpStatusCode.Created, create(body, admin).status)
        val duplicate = create(body, admin)
        assertEquals(HttpStatusCode.Conflict, duplicate.status)
        assertEquals("""{"message":"Already exists"}""", duplicate.bodyAsText())
    }

    @Test
    fun `only admins can create, update and delete challenges`() = testApplication {
        configure()
        startApplication()
        val admin = registerAdmin("write-admin")
        val player = register("write-player")
        assertEquals(HttpStatusCode.Created, create(challenge("protected"), admin).status)
        val id = challenges(admin).single { it.title == "protected" }.id

        for (token in listOf(null, player)) {
            val expected = if (token == null) HttpStatusCode.Unauthorized else HttpStatusCode.Forbidden
            assertEquals(expected, create(challenge("sneaky"), token).status)
            assertEquals(expected, client.put("/v1/challenges/$id") {
                token?.let { bearerAuth(it) }
                contentType(ContentType.Application.Json)
                setBody(challenge("renamed"))
            }.status)
            assertEquals(expected, client.delete("/v1/challenges/$id") { token?.let { bearerAuth(it) } }.status)
            assertEquals(expected, client.delete("/v1/challenges") {
                token?.let { bearerAuth(it) }
                contentType(ContentType.Application.Json)
                setBody("[$id]")
            }.status)
        }
        assertEquals(listOf("protected"), challenges(admin).filter { it.id == id }.map { it.title })
    }

    @Test
    fun `hidden and unpublished challenges are only shown to admins`() = testApplication {
        configure()
        startApplication()
        val admin = registerAdmin("challenge-admin")
        create(challenge("released"), admin)
        create(challenge("hidden", isVisible = false), admin)
        create(challenge("scheduled", publishAt = "2999-01-01T00:00:00Z"), admin)

        val public = challenges().map { it.title }
        assertTrue("released" in public)
        assertFalse("hidden" in public)
        assertFalse("scheduled" in public)
        assertTrue(challenges(admin).map { it.title }.containsAll(listOf("released", "hidden", "scheduled")))

        val hiddenId = challenges(admin).single { it.title == "hidden" }.id
        assertEquals(HttpStatusCode.NotFound, client.get("/v1/challenges/$hiddenId").status)
        assertEquals(HttpStatusCode.OK, client.get("/v1/challenges/$hiddenId") { bearerAuth(admin) }.status)
    }

    @Test
    fun `deleting a challenge deletes its flags, hints, unlocks and submissions`() = testApplication {
        configure()
        startApplication()
        val admin = registerAdmin("cascade-admin")
        create(challenge("cascading"), admin)
        val id = challenges(admin).single { it.title == "cascading" }.id

        // The app doesn't create these tables yet, so the test does.
        withTransaction {
            SchemaUtils.create(TeamTable, FlagTable, HintTable, HintUnlockTable, SubmissionTable)
            val userId = UserDAO.find { UserTable.username eq "cascade-admin" }.single().id
            val flagId = FlagTable.insertAndGetId {
                it[challengeId] = id
                it[pattern] = "flag{cascade}"
                it[isCaseSensitive] = true
                it[baseValue] = 100
            }
            val hintId = HintTable.insertAndGetId {
                it[challengeId] = id
                it[body] = "look closer"
            }
            HintUnlockTable.insert {
                it[HintUnlockTable.hintId] = hintId
                it[HintUnlockTable.userId] = userId
            }
            SubmissionTable.insert {
                it[SubmissionTable.userId] = userId
                it[challengeId] = id
                it[SubmissionTable.flagId] = flagId
                it[SubmissionTable.submittedFlag] = "flag{cascade}"
                it[SubmissionTable.isCorrect] = true
            }
        }

        assertEquals(HttpStatusCode.NoContent, client.delete("/v1/challenges/$id") { bearerAuth(admin) }.status)

        // Without the cascade the delete above fails on the foreign keys instead.
        val remaining = withTransaction {
            mapOf(
                "flags" to FlagTable.selectAll().where { FlagTable.challengeId eq id }.count(),
                "hints" to HintTable.selectAll().where { HintTable.challengeId eq id }.count(),
                "unlocks" to HintUnlockTable.selectAll().count(),
                "submissions" to SubmissionTable.selectAll().where { SubmissionTable.challengeId eq id }.count(),
            )
        }
        assertEquals(mapOf("flags" to 0L, "hints" to 0L, "unlocks" to 0L, "submissions" to 0L), remaining)
    }
}
