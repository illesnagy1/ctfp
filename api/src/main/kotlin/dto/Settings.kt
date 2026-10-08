package com.ctfp.dto

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val ctfName: String = "CTFP",
    val registrationOpen: Boolean = true,
    val maxTeamSize: Int = 4,
)
