package com.ctfp.service

import com.ctfp.dto.Settings
import kotlinx.coroutines.runBlocking
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class SettingsServiceTest {
    private val file = createTempDirectory("settings").resolve("nested/settings.conf")

    @Test
    fun `missing file yields defaults without creating it`() {
        assertEquals(Settings(), SettingsService(file).getSettings())
        assertFalse(file.exists())
    }

    @Test
    fun `update persists as hocon and survives a reload`() =
        runBlocking {
            val updated =
                Settings(
                    ctfName = "Test CTF",
                    registrationOpen = false,
                    maxTeamSize = 2,
                    startsAt = Instant.parse("2026-12-01T18:00:00Z"),
                    endsAt = Instant.parse("2026-12-03T18:00:00Z"),
                    freezeAt = Instant.parse("2026-12-03T17:00:00Z"),
                )
            SettingsService(file).updateSettings(updated)

            assertTrue("ctfName" in file.readText())
            assertEquals(updated, SettingsService(file).getSettings())
        }

    @Test
    fun `missing keys fall back to defaults`() {
        file.parent.toFile().mkdirs()
        file.writeText("ctfName = \"Partial\"\n")

        assertEquals(Settings(ctfName = "Partial"), SettingsService(file).getSettings())
    }

    @Test
    fun `invalid settings are rejected and not written`() =
        runBlocking {
            val service = SettingsService(file)

            // The @JsonSchema limits are checked on the request (see RequestValidationTest); a blank name passes those.
            assertFailsWith<ApiException> { service.updateSettings(Settings(ctfName = " ")) }
            assertFalse(file.exists())
            assertEquals(Settings(), service.getSettings())
        }
}
