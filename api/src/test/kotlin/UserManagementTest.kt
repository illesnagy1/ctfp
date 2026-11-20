package com.ctfp

import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.AccountExport
import com.ctfp.dto.AdminUserResponse
import com.ctfp.dto.UserResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The embedded database is shared between tests, so each one uses its own names. */
class UserManagementTest {
    private fun profile(
        name: String,
        email: String = "$name@example.com",
    ) = """{"username":"$name","email":"$email","language":null,"country":"HU","website":null,"visibilitySettings":null}"""

    private fun admin(
        name: String,
        role: String = "USER",
        isHidden: Boolean = false,
        password: String? = null,
    ) = """{"codeId":null,"username":"$name","email":"$name@example.com","language":null,"country":null,"website":null,
            "role":"$role","isHidden":$isHidden,"visibilitySettings":null${password?.let { ""","password":"$it"""" } ?: ""}}"""

    @Test
    fun `public profiles hide hidden, banned and missing users`() =
        testApplication {
            configure()
            startApplication()
            val viewer = register("profile-viewer")
            register("profile-visible")
            register("profile-hidden")
            register("profile-banned")
            withTransaction {
                UserDAO[userId("profile-hidden")].isHidden = true
                UserDAO[userId("profile-banned")].isBanned = true
            }

            val visible = call(HttpMethod.Get, "/v1/users/${userId("profile-visible")}", viewer)
            assertEquals(HttpStatusCode.OK, visible.status)
            assertEquals("profile-visible", Json.decodeFromString<UserResponse>(visible.bodyAsText()).username)
            for (name in listOf("profile-hidden", "profile-banned")) {
                assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Get, "/v1/users/${userId(name)}", viewer).status)
            }
            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Get, "/v1/users/${Int.MAX_VALUE}", viewer).status)
            val malformed = call(HttpMethod.Get, "/v1/users/abc", viewer)
            assertEquals(HttpStatusCode.BadRequest, malformed.status)
            assertEquals("Malformed request", malformed.problem().detail)

            val listed =
                Json
                    .decodeFromString<List<UserResponse>>(
                        call(HttpMethod.Get, "/v1/users", viewer).bodyAsText(),
                    ).map { it.username }
            assertTrue("profile-visible" in listed)
            assertFalse("profile-hidden" in listed)
            assertFalse("profile-banned" in listed)
        }

    @Test
    fun `users edit their own profile`() =
        testApplication {
            configure()
            startApplication()
            val token = register("self-edit")
            register("self-edit-taken")

            assertEquals(HttpStatusCode.NoContent, call(HttpMethod.Put, "/v1/users/me", token, profile("self-edit-renamed")).status)
            val me = Json.decodeFromString<AdminUserResponse>(call(HttpMethod.Get, "/v1/users/me", token).bodyAsText())
            assertEquals("self-edit-renamed", me.username)
            assertEquals("HU", me.country)

            assertEquals(HttpStatusCode.Conflict, call(HttpMethod.Put, "/v1/users/me", token, profile("self-edit-taken")).status)
            assertEquals(HttpStatusCode.BadRequest, call(HttpMethod.Put, "/v1/users/me", token, profile("x")).status)
            assertEquals(HttpStatusCode.BadRequest, call(HttpMethod.Put, "/v1/users/me", token, profile("self-edit-ok", "nope")).status)
        }

    @Test
    fun `admins create, read, update and delete users`() =
        testApplication {
            configure()
            startApplication()
            val token = registerAdmin("crud-admin")

            val created = call(HttpMethod.Post, "/v1/admin/users", token, admin("crud-user", password = "correct-horse"))
            assertEquals(HttpStatusCode.Created, created.status)
            val login = call(HttpMethod.Post, "/v1/auth/login", body = """{"login":"crud-user","password":"correct-horse"}""")
            assertEquals(HttpStatusCode.OK, login.status)
            assertEquals(
                HttpStatusCode.Conflict,
                call(HttpMethod.Post, "/v1/admin/users", token, admin("crud-user", password = "correct-horse")).status,
            )
            assertEquals(
                HttpStatusCode.BadRequest,
                call(HttpMethod.Post, "/v1/admin/users", token, admin("crud-weak", password = "short")).status,
            )
            assertEquals(
                HttpStatusCode.BadRequest,
                call(HttpMethod.Post, "/v1/admin/users", token, admin("!", password = "correct-horse")).status,
            )

            val id = userId("crud-user")
            val read = call(HttpMethod.Get, "/v1/admin/users/$id", token)
            assertEquals(HttpStatusCode.OK, read.status)
            assertEquals("crud-user@example.com", Json.decodeFromString<AdminUserResponse>(read.bodyAsText()).email)
            val all = Json.decodeFromString<List<AdminUserResponse>>(call(HttpMethod.Get, "/v1/admin/users", token).bodyAsText())
            assertTrue(all.any { it.id == id })

            assertEquals(
                HttpStatusCode.NoContent,
                call(HttpMethod.Put, "/v1/admin/users/$id", token, admin("crud-user", isHidden = true)).status,
            )
            assertTrue(withTransaction { UserDAO[id].isHidden })
            assertEquals(
                HttpStatusCode.NotFound,
                call(HttpMethod.Put, "/v1/admin/users/${Int.MAX_VALUE}", token, admin("crud-ghost")).status,
            )

            assertEquals(HttpStatusCode.NoContent, call(HttpMethod.Delete, "/v1/admin/users/$id", token).status)
            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Get, "/v1/admin/users/$id", token).status)
            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Delete, "/v1/admin/users/$id", token).status)
        }

    @Test
    fun `bulk user updates are all or nothing`() =
        testApplication {
            configure()
            startApplication()
            val token = registerAdmin("bulk-user-admin")
            register("bulk-user-a")
            register("bulk-user-b")
            val (a, b) = listOf(userId("bulk-user-a"), userId("bulk-user-b"))

            val both = """{"$a":${admin("bulk-user-a2")},"$b":${admin("bulk-user-b2", role = "ADMIN")}}"""
            assertEquals(HttpStatusCode.NoContent, call(HttpMethod.Put, "/v1/admin/users", token, both).status)
            val missing = """{"$a":${admin("bulk-user-a3")},"${Int.MAX_VALUE}":${admin("bulk-user-ghost")}}"""
            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Put, "/v1/admin/users", token, missing).status)
            // Two users taking the same name in one request hit the unique index.
            val clash = """{"$a":${admin("bulk-user-same")},"$b":${admin("bulk-user-same", role = "ADMIN")}}"""
            assertEquals(HttpStatusCode.Conflict, call(HttpMethod.Put, "/v1/admin/users", token, clash).status)

            assertEquals(listOf("bulk-user-a2", "bulk-user-b2"), withTransaction { listOf(UserDAO[a].username, UserDAO[b].username) })
        }

    @Test
    fun `a banned admin's token stops working`() =
        testApplication {
            configure()
            startApplication()
            val token = registerAdmin("banned-admin")
            assertEquals(HttpStatusCode.OK, call(HttpMethod.Get, "/v1/admin/users", token).status)

            withTransaction { UserDAO[userId("banned-admin")].isBanned = true }
            assertEquals(HttpStatusCode.Unauthorized, call(HttpMethod.Get, "/v1/admin/users", token).status)
        }

    @Test
    fun `a banned user's token is rejected`() =
        testApplication {
            configure()
            startApplication()
            val token = register("banned-player")
            assertEquals(HttpStatusCode.OK, call(HttpMethod.Get, "/v1/users/me", token).status)

            withTransaction { UserDAO[userId("banned-player")].isBanned = true }
            assertEquals(HttpStatusCode.Unauthorized, call(HttpMethod.Get, "/v1/users/me", token).status)
        }

    @Test
    fun `players export and delete their own account`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("delete-admin")
            val token = register("delete-me")
            val id = challengeWithFlag(admin, "delete-me-challenge", """{"pattern":"delete-flag","baseValue":1}""")
            call(HttpMethod.Post, "/v1/challenges/$id/submissions", token, """{"flag":"delete-flag"}""")

            val export = Json.decodeFromString<AccountExport>(call(HttpMethod.Get, "/v1/users/me/export", token).bodyAsText())
            assertEquals("delete-me", export.account.username)
            assertEquals(1, export.submissions.size)

            val wrong = call(HttpMethod.Delete, "/v1/users/me", token, """{"password":"not-the-password"}""")
            assertEquals(HttpStatusCode.Forbidden, wrong.status)
            val deleted = call(HttpMethod.Delete, "/v1/users/me", token, """{"password":"correct-horse"}""")
            assertEquals(HttpStatusCode.NoContent, deleted.status)
            assertEquals(HttpStatusCode.Unauthorized, call(HttpMethod.Get, "/v1/users/me", token).status)
        }
}
