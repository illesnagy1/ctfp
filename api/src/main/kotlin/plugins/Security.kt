package com.ctfp.plugins

import io.ktor.server.application.*
import com.auth0.jwt.JWT
import com.ctfp.dto.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.ctfp.config.jwtConfig

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
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Authentication required"))
            }
        }
    }
}

/** Id of the authenticated user; only call inside an `authenticate { }` block. */
fun ApplicationCall.userId(): Int = principal<JWTPrincipal>()!!.subject!!.toInt()
