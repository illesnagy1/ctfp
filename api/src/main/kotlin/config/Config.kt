package com.ctfp.config

import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.getAs
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@Serializable
data class JwtConfig(
    val issuer: String,
    val audience: String,
    val realm: String,
    val secret: String,
    val ttlHours: Long,
) {
    val ttl: Duration get() = ttlHours.hours
}

@Serializable
data class PostgresConfig(
    val embedded: Boolean,
    val url: String,
    val user: String,
    val password: String,
)

@Serializable
data class SettingsFileConfig(
    val file: String,
)

@Serializable
data class AppConfig(
    val jwt: JwtConfig,
    val postgres: PostgresConfig,
    val settings: SettingsFileConfig,
)

fun ApplicationConfig.jwtConfig(): JwtConfig = property("jwt").getAs()

fun ApplicationConfig.postgresConfig(): PostgresConfig = property("postgres").getAs()

fun ApplicationConfig.settingsFileConfig(): SettingsFileConfig = property("settings").getAs()

fun ApplicationConfig.appConfig() = AppConfig(
    jwt = jwtConfig(),
    postgres = postgresConfig(),
    settings = settingsFileConfig(),
)
