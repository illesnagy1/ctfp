package com.ctfp

import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.AuthResponse
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.eq
import kotlin.test.assertEquals

/** Registers `name` (with `name@example.com`) and returns its token. The embedded database is shared between tests, so use unique names. */
suspend fun ApplicationTestBuilder.register(name: String): String {
    val response =
        client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"$name","email":"$name@example.com","password":"correct-horse"}""")
        }
    assertEquals(HttpStatusCode.Created, response.status)
    return Json.decodeFromString<AuthResponse>(response.bodyAsText()).token
}

/** Registers a user and promotes it in the database; there's no api for becoming the first admin. Needs `startApplication()` first. */
suspend fun ApplicationTestBuilder.registerAdmin(name: String): String {
    val token = register(name)
    withTransaction { UserDAO.find { UserTable.username eq name }.single().role = UserRole.ADMIN }
    return token
}

suspend fun userId(name: String): Int =
    withTransaction {
        UserDAO
            .find { UserTable.username eq name }
            .single()
            .id.value
    }
