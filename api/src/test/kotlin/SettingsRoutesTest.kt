package com.ctfp

import com.ctfp.dto.AuthResponse
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRoutesTest {
    @Test
    fun `settings are public but only admins can update them`() = testApplication {
        configure()
        val register = client.post("/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"settings-user","email":"settings-user@example.com","password":"correct-horse"}""")
        }
        val token = Json.decodeFromString<AuthResponse>(register.bodyAsText()).token
        val body = """{"ctfName":"Hacked"}"""

        assertEquals(HttpStatusCode.OK, client.get("/v1/settings").status)
        assertEquals(HttpStatusCode.Unauthorized, client.put("/v1/admin/settings") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.status)
        assertEquals(HttpStatusCode.Forbidden, client.put("/v1/admin/settings") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(body)
        }.status)
    }
}
