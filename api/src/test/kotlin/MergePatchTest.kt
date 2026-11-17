package com.ctfp

import com.ctfp.dto.AdminUserResponse
import com.ctfp.plugins.MergePatchJson
import com.ctfp.service.helper.mergePatch
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class MergePatchTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun merge(
        target: String,
        patch: String,
    ) = Json.parseToJsonElement(target).mergePatch(Json.parseToJsonElement(patch)).toString()

    @Test
    fun `merges as RFC 7396 says`() {
        // From the RFC's Appendix A.
        assertEquals("""{"a":"c"}""", merge("""{"a":"b"}""", """{"a":"c"}"""))
        assertEquals("""{"a":"b","b":"c"}""", merge("""{"a":"b"}""", """{"b":"c"}"""))
        assertEquals("""{}""", merge("""{"a":"b"}""", """{"a":null}"""))
        assertEquals("""{"a":{"b":"d"}}""", merge("""{"a":{"b":"c"}}""", """{"a":{"b":"d","c":null}}"""))
        assertEquals("""{"a":["b"]}""", merge("""{"a":[{"b":"c"}]}""", """{"a":["b"]}"""))
        assertEquals("""["c"]""", merge("""{"a":"foo"}""", """["c"]"""))
        assertEquals("""{"a":"foo"}""", merge("""{"a":"foo"}""", """{"b":null}"""))
    }

    private suspend fun ApplicationTestBuilder.patch(
        path: String,
        token: String,
        body: String,
    ) = client.patch(path) {
        bearerAuth(token)
        contentType(MergePatchJson)
        setBody(body)
    }

    private suspend fun ApplicationTestBuilder.adminView(
        admin: String,
        id: Int,
    ) = json.decodeFromString<AdminUserResponse>(client.get("/v1/admin/users/$id") { bearerAuth(admin) }.bodyAsText())

    @Test
    fun `admins change only the fields they send`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("patch-admin")
            val player = register("patch-player")
            val id = userId("patch-player")

            assertEquals(HttpStatusCode.Forbidden, patch("/v1/admin/users/$id", player, """{"isBanned":true}""").status)
            assertEquals(HttpStatusCode.NoContent, patch("/v1/admin/users/$id", admin, """{"isBanned":true}""").status)
            val banned = adminView(admin, id)
            assertEquals(true, banned.isBanned)
            assertEquals("patch-player", banned.username)

            val invalid = patch("/v1/admin/users/$id", admin, """{"username":"x"}""")
            assertEquals(
                listOf("#/username"),
                invalid
                    .problem()
                    .errors
                    ?.map { it.pointer }
                    ?.distinct(),
            )
            assertEquals(HttpStatusCode.BadRequest, patch("/v1/admin/users/$id", admin, """{"nickname":"x"}""").status)
            assertEquals(HttpStatusCode.BadRequest, patch("/v1/admin/users/$id", admin, """{"username":null}""").status)
        }

    @Test
    fun `players patch their own profile, null clearing a field`() =
        testApplication {
            configure()
            val token = register("patch-self")
            assertEquals(
                HttpStatusCode.NoContent,
                patch("/v1/users/me", token, """{"country":"HU","website":"https://example.com"}""").status,
            )
            assertEquals(HttpStatusCode.NoContent, patch("/v1/users/me", token, """{"website":null}""").status)

            val me = json.decodeFromString<AdminUserResponse>(client.get("/v1/users/me") { bearerAuth(token) }.bodyAsText())
            assertEquals("HU" to null, me.country to me.website)
            // Account fields aren't part of the profile.
            assertEquals(HttpStatusCode.BadRequest, patch("/v1/users/me", token, """{"isBanned":false}""").status)
        }

    @Test
    fun `bulk patches are keyed by id, with pointers to match`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("bulk-patch-admin")
            register("bulk-patch-a")
            register("bulk-patch-b")
            val (a, b) = userId("bulk-patch-a") to userId("bulk-patch-b")

            val body = """{"$a":{"isHidden":true},"$b":{"isHidden":true}}"""
            assertEquals(HttpStatusCode.NoContent, patch("/v1/admin/users", admin, body).status)
            assertEquals(listOf(true, true), listOf(a, b).map { adminView(admin, it).isHidden })

            val invalid = patch("/v1/admin/users", admin, """{"$b":{"email":"nope"}}""")
            assertEquals(listOf("#/$b/email"), invalid.problem().errors?.map { it.pointer })
            assertEquals(HttpStatusCode.NotFound, patch("/v1/admin/users", admin, """{"999999":{"isHidden":true}}""").status)
        }

    @Test
    fun `parallel patches to one user all apply`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("race-patch-admin")
            register("race-patch-target")
            val id = userId("race-patch-target")

            // Each patch changes a different field; without the row lock, one would write back the others' old values.
            val patches =
                listOf(
                    """{"isHidden":true}""",
                    """{"isVerified":true}""",
                    """{"country":"HU"}""",
                    """{"website":"https://example.com"}""",
                    """{"language":"hu"}""",
                )
            coroutineScope { patches.map { async { patch("/v1/admin/users/$id", admin, it).status } }.awaitAll() }
                .forEach { assertEquals(HttpStatusCode.NoContent, it) }

            val user = adminView(admin, id)
            assertEquals(
                listOf(true, true, "HU", "https://example.com", "hu"),
                listOf(user.isHidden, user.isVerified, user.country, user.website, user.language),
            )
            assertEquals(patches.size, user.version)
        }

    @Test
    fun `If-Match refuses edits of a changed user`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("version-admin")
            register("version-target")
            val id = userId("version-target")

            suspend fun patchIfMatch(
                etag: String?,
                body: String,
            ) = client.patch("/v1/admin/users/$id") {
                bearerAuth(admin)
                contentType(MergePatchJson)
                etag?.let { header(HttpHeaders.IfMatch, it) }
                setBody(body)
            }

            val read = client.get("/v1/admin/users/$id") { bearerAuth(admin) }.headers[HttpHeaders.ETag]
            val written = patchIfMatch(read, """{"isHidden":true}""")
            assertEquals(HttpStatusCode.NoContent, written.status)

            // Someone else's edit was based on the old version.
            val stale = patchIfMatch(read, """{"isHidden":false}""")
            assertEquals(HttpStatusCode.PreconditionFailed, stale.status)
            assertEquals("This user was changed in the meantime; reload and try again", stale.problem().detail)
            assertEquals(true, adminView(admin, id).isHidden)

            assertEquals(HttpStatusCode.NoContent, patchIfMatch("*", """{"isVerified":true}""").status)
            assertEquals(HttpStatusCode.NoContent, patchIfMatch(null, """{"isVerified":false}""").status)
        }
}
