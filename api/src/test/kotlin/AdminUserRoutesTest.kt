package com.ctfp

import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class AdminUserRoutesTest {
    @Test
    fun `admin routes reject non-admins and anonymous calls`() =
        testApplication {
            configure()
            val token = register("not-an-admin")

            assertEquals(HttpStatusCode.Unauthorized, client.get("/v1/admin/users").status)
            assertEquals(HttpStatusCode.Forbidden, client.get("/v1/admin/users") { bearerAuth(token) }.status)
            assertEquals(HttpStatusCode.Forbidden, client.get("/v1/admin/users/1") { bearerAuth(token) }.status)
        }

    @Test
    fun `users can read the public list and their own account`() =
        testApplication {
            configure()
            val token = register("player")

            assertEquals(HttpStatusCode.OK, client.get("/v1/users") { bearerAuth(token) }.status)
            val me = client.get("/v1/users/me") { bearerAuth(token) }
            assertEquals(HttpStatusCode.OK, me.status)
            assertEquals(true, "player@example.com" in me.bodyAsText())
        }

    private fun ban(
        name: String,
        role: String,
    ) = update(name, role, isBanned = true)

    private fun update(
        name: String,
        role: String,
        isBanned: Boolean = false,
    ) = """{"codeId":null,"username":"$name","email":"$name@example.com","language":null,"country":null,
            "website":null,"role":"$role","isBanned":$isBanned,"visibilitySettings":null}"""

    private suspend fun ApplicationTestBuilder.put(
        path: String,
        token: String,
        body: String,
    ) = client.put(path) {
        bearerAuth(token)
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    @Test
    fun `admins can ban others but not themselves`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("self-ban-admin")
            register("ban-target")
            val adminId = userId("self-ban-admin")
            val targetId = userId("ban-target")

            val single = put("/v1/admin/users/$adminId", admin, ban("self-ban-admin", "ADMIN"))
            assertEquals(HttpStatusCode.BadRequest, single.status)
            assertEquals("""{"message":"You cannot ban yourself"}""", single.bodyAsText())

            // All or nothing: the other user in the same bulk update isn't banned either.
            val bulk =
                put(
                    "/v1/admin/users",
                    admin,
                    """{"$adminId":${ban("self-ban-admin", "ADMIN")},"$targetId":${ban("ban-target", "USER")}}""",
                )
            assertEquals(HttpStatusCode.BadRequest, bulk.status)
            assertEquals(false, "\"isBanned\":true" in client.get("/v1/admin/users/$targetId") { bearerAuth(admin) }.bodyAsText())

            assertEquals(HttpStatusCode.NoContent, put("/v1/admin/users/$targetId", admin, ban("ban-target", "USER")).status)
            assertEquals(HttpStatusCode.OK, client.get("/v1/admin/users") { bearerAuth(admin) }.status)
        }

    @Test
    fun `admins can change others' roles but not their own`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("self-demote-admin")
            register("promote-target")
            val adminId = userId("self-demote-admin")
            val targetId = userId("promote-target")

            val single = put("/v1/admin/users/$adminId", admin, update("self-demote-admin", "USER"))
            assertEquals(HttpStatusCode.BadRequest, single.status)
            assertEquals("""{"message":"You cannot change your own role"}""", single.bodyAsText())

            // All or nothing: the other user in the same bulk update isn't promoted either.
            val bulk =
                put(
                    "/v1/admin/users",
                    admin,
                    """{"$adminId":${update("self-demote-admin", "USER")},"$targetId":${update("promote-target", "ADMIN")}}""",
                )
            assertEquals(HttpStatusCode.BadRequest, bulk.status)
            assertEquals(false, "\"role\":\"ADMIN\"" in client.get("/v1/admin/users/$targetId") { bearerAuth(admin) }.bodyAsText())

            // Saving their own account unchanged still works.
            assertEquals(HttpStatusCode.NoContent, put("/v1/admin/users/$adminId", admin, update("self-demote-admin", "ADMIN")).status)
            assertEquals(HttpStatusCode.NoContent, put("/v1/admin/users/$targetId", admin, update("promote-target", "ADMIN")).status)
        }
}
