package com.ctfp.plugins

import com.ctfp.config.enum.UserRole
import com.ctfp.dto.AdminUserUpdateRequest
import com.ctfp.dto.Limits
import com.ctfp.dto.Settings
import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class RequestValidationTest {
    @Serializable
    data class Item(
        @JsonSchema.MaxLength(3)
        val name: String,
    )

    @Serializable
    data class Body(
        @JsonSchema.Minimum(0.0, exclusive = true)
        val count: Int,
        @JsonSchema.MinLength(1)
        val note: String? = null,
        val items: List<Item> = emptyList(),
    )

    private fun user(
        username: String,
        email: String = "a@b.co",
    ) = AdminUserUpdateRequest(
        codeId = null,
        username = username,
        email = email,
        language = null,
        country = null,
        website = null,
        role = UserRole.USER,
        visibilitySettings = null,
    )

    private fun messages(body: Any) = violations(body).map { it.message }

    @Test
    fun `valid bodies and non-DTO bodies pass`() {
        assertEquals(emptyList(), messages(Settings()))
        assertEquals(emptyList(), messages(Body(count = 1, items = listOf(Item("abc")))))
        assertEquals(emptyList(), messages("any text"))
        assertEquals(emptyList(), messages(listOf(1, 2)))
    }

    @Test
    fun `every broken limit is reported, nested ones with their path`() {
        assertEquals(
            listOf("ctfName must be at least 1 characters", "maxTeamSize must be at most ${Settings.MAX_TEAM_SIZE}"),
            messages(Settings(ctfName = "", maxTeamSize = Settings.MAX_TEAM_SIZE + 1)),
        )
        assertEquals(
            listOf("count must be more than 0", "note must be at least 1 characters", "items[1].name must be at most 3 characters"),
            messages(Body(count = 0, note = "", items = listOf(Item("ok"), Item("long")))),
        )
    }

    @Test
    fun `patterns, emails and code point lengths`() {
        assertEquals(
            listOf("username must match ${Limits.USERNAME_PATTERN}", "email must be an email address"),
            messages(user("no spaces", email = "nope")),
        )
        // 3 emoji are 6 UTF-16 chars but 3 characters, as JSON Schema counts them.
        assertEquals(emptyList(), messages(Item("😀😀😀")))
    }

    @Test
    fun `bulk bodies check each value, keyed by id`() {
        val violations = violations(mapOf(1 to user("ok-name"), 2 to user("x!y")))
        assertEquals(listOf("[2].username must match ${Limits.USERNAME_PATTERN}"), violations.map { it.message })
        assertEquals(listOf("#/2/username"), violations.map { it.pointer })
    }

    @Test
    fun `pointers follow RFC 6901`() {
        assertEquals("#/items/1/name", Violation(listOf("items", "1", "name"), "x").pointer)
        assertEquals("#/a~1b/c~0d", Violation(listOf("a/b", "c~d"), "x").pointer)
    }
}
