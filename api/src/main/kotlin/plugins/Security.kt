package com.ctfp.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ctfp.config.enum.UserRole
import com.ctfp.config.jwtConfig
import com.ctfp.dto.ErrorResponse
import com.ctfp.service.helper.isAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.AuthenticationSchemeWithRoles
import io.ktor.server.auth.ForbiddenHandler
import io.ktor.server.auth.OptionalPrincipalContext
import io.ktor.server.auth.PrincipalContext
import io.ktor.server.auth.RolesContext
import io.ktor.server.auth.SimpleAuthenticationScheme
import io.ktor.server.auth.authenticateWith
import io.ktor.server.auth.authenticateWithOptional
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.roles
import io.ktor.server.auth.withRoles
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import io.ktor.util.AttributeKey

private const val MIN_JWT_SECRET_LENGTH = 32

/** The caller; read it with `call.principal` (or `call.principalOrNull`) inside the route builders below. */
data class UserPrincipal(
    val id: Int,
)

private typealias RoleAuth =
    AuthenticationSchemeWithRoles<UserPrincipal, UserRole, Unit, SimpleAuthenticationScheme<UserPrincipal>>

private val RoleAuthKey = AttributeKey<RoleAuth>("RoleAuth")

private val Route.roleAuth: RoleAuth get() = application.attributes[RoleAuthKey]

fun Application.configureSecurity() {
    val jwtConfig = environment.config.jwtConfig()
    require(jwtConfig.secret.length >= MIN_JWT_SECRET_LENGTH) { "jwt.secret (JWT_SECRET) must be at least 32 characters" }

    val jwtAuth =
        jwt<UserPrincipal>("jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build(),
            )
            validate { credential ->
                credential.payload.subject
                    ?.toIntOrNull()
                    ?.let(::UserPrincipal)
            }
            onUnauthorized = { call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Authentication required")) }
        }
    // Roles come from the database on every call, so promotions and bans apply without a new token.
    val roleAuth =
        jwtAuth.withRoles(
            onForbidden =
                ForbiddenHandler<UserPrincipal, Unit, UserRole> {
                    call.respond(
                        HttpStatusCode.Forbidden,
                        ErrorResponse("Admin access required"),
                    )
                },
        ) { setOf(if (isAdmin(it.id)) UserRole.ADMIN else UserRole.USER) }
    attributes.put(RoleAuthKey, roleAuth)
}

/**
 * Body of [authenticated] (logged-in callers, 401 otherwise) and [adminOnly] (also 403 for non-admins, banned admins
 * included); `call.principal` is the caller.
 */
typealias UserRoutes = context(PrincipalContext<UserPrincipal>, Unit, RolesContext<UserPrincipal, UserRole>)
Route.() -> Unit

/**
 * Body of [optionallyAuthenticated] (logging in is optional); `call.principalOrNull` is the caller, null for guests.
 * OpenAPI inference doesn't see typed auth as optional, so the builder describes it.
 */
typealias OptionalUserRoutes =
    context(OptionalPrincipalContext<UserPrincipal>, Unit, RolesContext<UserPrincipal, UserRole>)
    Route.() -> Unit

// No comments directly above these: the OpenAPI compiler plugin would copy them into every route's summary.
fun Route.authenticated(build: UserRoutes): Route = authenticateWith(roleAuth, build = build)

fun Route.adminOnly(build: UserRoutes): Route = authenticateWith(roleAuth, roles = setOf(UserRole.ADMIN), build = build)

fun Route.optionallyAuthenticated(build: OptionalUserRoutes): Route =
    authenticateWithOptional(roleAuth, build = build).describe { security { optional() } }

/** Whether the caller is a logged-in, not banned admin. */
context(_: RolesContext<UserPrincipal, UserRole>, _: RoutingContext)
val UserPrincipal?.isAdmin: Boolean get() = this != null && UserRole.ADMIN in roles
