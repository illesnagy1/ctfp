package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import kotlinx.serialization.Serializable

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
) {
    companion object {
        const val MAX_CTF_NAME_LENGTH = 64
        const val MAX_TEAM_SIZE = 100
    }
}
