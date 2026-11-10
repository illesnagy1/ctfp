package com.ctfp.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ctfp.config.jwtConfig
import com.ctfp.dto.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.response.respond

private const val MIN_JWT_SECRET_LENGTH = 32

fun Application.configureSecurity() {
    val jwtConfig = environment.config.jwtConfig()
    require(jwtConfig.secret.length >= MIN_JWT_SECRET_LENGTH) { "jwt.secret (JWT_SECRET) must be at least 32 characters" }

    authentication {
        jwt {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build(),
            )
            validate { credential ->
                if (credential.payload.subject?.toIntOrNull() != null) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Authentication required"))
            }
        }
    }
}

/** Id of the authenticated user; only call inside an `authenticate { }` block. */
fun ApplicationCall.userId(): Int = principal<JWTPrincipal>()!!.subject!!.toInt()
