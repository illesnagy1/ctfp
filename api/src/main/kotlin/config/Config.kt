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

/** Requests allowed per window; see plugins/RateLimiting.kt. */
@Serializable
data class RateLimitConfig(
    /** Any requests per client IP, every 10 seconds. */
    val global: Int,
    /** Logins, registrations and password resets per client IP, every minute. */
    val auth: Int,
    /** Flag submissions per player, every minute. */
    val submissions: Int,
)

@Serializable
data class UploadsConfig(
    /** Directory the uploaded files are stored in, under random names. */
    val dir: String,
    /** Largest accepted upload. */
    val maxBytes: Long,
)

@Serializable
data class SmtpConfig(
    val host: String,
    val security: String,
    /** 0: the usual port for [security]. */
    val port: Int,
    val username: String,
    val password: String,
) {
    override fun toString() = "SmtpConfig(host=$host, security=$security, port=$port, username=$username)"
}

@Serializable
data class MailConfig(
    /** `Name <address>` or a bare address. */
    val from: String,
    /** The frontend's origin, e.g. `https://ctf.example.com`; links in emails point there. */
    val publicUrl: String,
    val smtp: SmtpConfig,
)

@Serializable
data class AppConfig(
    val jwt: JwtConfig,
    val postgres: PostgresConfig,
    val settings: SettingsFileConfig,
    val uploads: UploadsConfig,
)

fun ApplicationConfig.jwtConfig(): JwtConfig = property("jwt").getAs()

fun ApplicationConfig.postgresConfig(): PostgresConfig = property("postgres").getAs()

fun ApplicationConfig.settingsFileConfig(): SettingsFileConfig = property("settings").getAs()

fun ApplicationConfig.uploadsConfig(): UploadsConfig = property("uploads").getAs()

fun ApplicationConfig.mailConfig(): MailConfig = property("mail").getAs()

fun ApplicationConfig.rateLimitConfig(): RateLimitConfig = property("rateLimit").getAs()

fun ApplicationConfig.appConfig() =
    AppConfig(
        jwt = jwtConfig(),
        postgres = postgresConfig(),
        settings = settingsFileConfig(),
        uploads = uploadsConfig(),
    )
