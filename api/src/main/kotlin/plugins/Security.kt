package com.ctfp.plugins

import io.ktor.server.application.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.ApplicationConfig
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data class JwtConfig(
    val issuer: String,
    val audience: String,
    val realm: String,
    val secret: String,
    val ttl: Duration,
)

fun ApplicationConfig.jwtConfig() = JwtConfig(
    issuer = property("jwt.issuer").getString(),
    audience = property("jwt.audience").getString(),
    realm = property("jwt.realm").getString(),
    secret = property("jwt.secret").getString(),
    ttl = property("jwt.ttlHours").getString().toLong().hours,
)

fun Application.configureSecurity() {
    val jwtConfig = environment.config.jwtConfig()
    require(jwtConfig.secret.length >= 32) { "jwt.secret (JWT_SECRET) must be at least 32 characters" }

    authentication {
        jwt {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.subject?.toIntOrNull() != null) JWTPrincipal(credential.payload) else null
            }
        }
    }
}

/** Id of the authenticated user; only call inside an `authenticate { }` block. */
fun ApplicationCall.userId(): Int = principal<JWTPrincipal>()!!.subject!!.toInt()
