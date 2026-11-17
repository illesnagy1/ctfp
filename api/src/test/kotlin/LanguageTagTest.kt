package com.ctfp

import com.ctfp.dto.AdminUserResponse
import com.ctfp.dto.Limits
import com.ctfp.dto.canonicalLanguageTag
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LanguageTagTest {
    private val pattern = Regex(Limits.LANGUAGE_TAG_PATTERN)

    @Test
    fun `accepts BCP 47 tags, up to variants`() {
        for (tag in listOf("en", "hu", "fil", "pt-BR", "zh-Hant", "zh-Hant-TW", "es-419", "sl-rozaj", "de-CH-1901")) {
            assertTrue(pattern.matches(tag), tag)
        }
        for (tag in listOf("", "e", "english", "en_US", "en-", "-en", "en-US-x-private", "en-u-ca-gregory", "12")) {
            assertFalse(pattern.matches(tag), tag)
        }
    }

    @Test
    fun `stores tags in their conventional case`() {
        assertEquals("zh-Hant-TW", canonicalLanguageTag("ZH-hant-tw"))
        assertEquals("es-419", canonicalLanguageTag("ES-419"))
        assertEquals("de-CH-1901", canonicalLanguageTag("de-ch-1901"))
    }

    @Test
    fun `a profile's language is a validated, canonical tag`() =
        testApplication {
            configure()
            val token = register("language-player")

            fun profile(language: String) =
                """{"username":"language-player","email":"language-player@example.com","language":"$language",""" +
                    """"country":null,"website":null,"visibilitySettings":null}"""

            assertEquals(HttpStatusCode.NoContent, call(HttpMethod.Put, "/v1/users/me", token, profile("pt-br")).status)
            val me =
                Json {
                    ignoreUnknownKeys = true
                }.decodeFromString<AdminUserResponse>(client.get("/v1/users/me") { bearerAuth(token) }.bodyAsText())
            assertEquals("pt-BR", me.language)

            val refused = call(HttpMethod.Put, "/v1/users/me", token, profile("portuguese"))
            assertEquals(listOf("#/language"), refused.problem().errors?.map { it.pointer })
        }
}
