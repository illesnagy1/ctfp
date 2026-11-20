package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Platform-wide settings, edited on the admin config page. That page is generated from this class's schema in
 * openapi.json: descriptions become help texts and the limits become validation; labels are translated in the frontend
 * (`settings_<field>` messages). The limits only document, so SettingsService checks the same ones.
 */
@Serializable
data class Settings(
    @JsonSchema.Description("Shown in the page title and the header.")
    @JsonSchema.MinLength(1)
    @JsonSchema.MaxLength(MAX_CTF_NAME_LENGTH)
    val ctfName: String = "CTFP",
    @JsonSchema.Description("Whether new players can create accounts.")
    val registrationOpen: Boolean = true,
    @JsonSchema.Description("The most players a team can have.")
    @JsonSchema.Minimum(1.0)
    @JsonSchema.Maximum(MAX_TEAM_SIZE.toDouble())
    val maxTeamSize: Int = 4,
    @JsonSchema.Description("When players can see challenges and submit flags, in UTC (e.g. 2026-12-01T18:00:00Z); empty: already open.")
    val startsAt: Instant? = null,
    @JsonSchema.Description("When flags stop being accepted, in UTC; empty: never.")
    val endsAt: Instant? = null,
    @JsonSchema.Description("The public scoreboard and challenge stats only count solves made before this, in UTC; empty: always live.")
    val freezeAt: Instant? = null,
) {
    companion object {
        const val MAX_CTF_NAME_LENGTH = 64
        const val MAX_TEAM_SIZE = 100
    }
}
