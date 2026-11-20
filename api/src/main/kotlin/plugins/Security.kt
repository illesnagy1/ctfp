package com.ctfp.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ctfp.config.enum.UserRole
import com.ctfp.config.jwtConfig
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.service.helper.audit
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.application.install
import io.ktor.server.auth.AuthenticationSchemeWithRoles
import io.ktor.server.auth.ForbiddenHandler
import io.ktor.server.auth.OptionalPrincipalContext
import io.ktor.server.auth.PrincipalContext
import io.ktor.server.auth.RolesContext
import io.ktor.server.auth.SimpleAuthenticationScheme
import io.ktor.server.auth.authenticateWith
import io.ktor.server.auth.authenticateWithOptional
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.auth.roles
import io.ktor.server.auth.withRoles
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.application
import io.ktor.server.routing.openapi.describe
import io.ktor.util.AttributeKey
import org.slf4j.event.Level

private const val MIN_JWT_SECRET_LENGTH = 32

/** application.conf's fallback for local development; anyone can read it, so anyone could sign tokens with it. */
private const val DEV_JWT_SECRET = "dev-only-insecure-jwt-secret-change-me"

/** The caller; read it with `call.principal` (or `call.principalOrNull`) inside the route builders below. */
data class UserPrincipal(
    val id: Int,
    val role: UserRole,
)

private typealias RoleAuth =
    AuthenticationSchemeWithRoles<UserPrincipal, UserRole, Unit, SimpleAuthenticationScheme<UserPrincipal>>

private val RoleAuthKey = AttributeKey<RoleAuth>("RoleAuth")

private val Route.roleAuth: RoleAuth get() = application.attributes[RoleAuthKey]

fun Application.configureSecurity() {
    val jwtConfig = environment.config.jwtConfig()
    // RFC 8725 §3.5: HS256 needs a key of at least 256 bits, and a secret one.
    require(jwtConfig.secret.length >= MIN_JWT_SECRET_LENGTH) { "jwt.secret (JWT_SECRET) must be at least 32 characters" }
    require(developmentMode || jwtConfig.secret != DEV_JWT_SECRET) {
        "Set JWT_SECRET to 32+ random characters; the built-in secret is only for development (KTOR_DEVELOPMENT=true)"
    }

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
            // The user is read on every call, so bans, deletions and role changes apply without a new token.
            validate { credential ->
                val id = credential.payload.subject?.toIntOrNull() ?: return@validate null
                withTransaction {
                    UserDAO.findById(id)?.takeUnless { it.isBanned }?.let { UserPrincipal(id, it.role) }
                }
            }
            onUnauthorized = {
                if (call.request.headers.contains(HttpHeaders.Authorization)) {
                    audit("authn_token_invalid", "path" to call.request.path(), level = Level.WARN)
                }
                call.respondProblem(HttpStatusCode.Unauthorized, "Authentication required")
            }
        }
    val roleAuth =
        jwtAuth.withRoles(
            onForbidden =
                ForbiddenHandler<UserPrincipal, Unit, UserRole> {
                    audit("authz_fail", "user" to call.principal<UserPrincipal>()?.id, "path" to call.request.path(), level = Level.WARN)
                    call.respondProblem(HttpStatusCode.Forbidden, "Admin access required")
                },
        ) { setOf(it.role) }
    attributes.put(RoleAuthKey, roleAuth)
}

/**
 * Body of [authenticated] (logged-in callers, 401 otherwise) and [adminOnly] (also 403 for non-admins); banned or
 * deleted users get 401; `call.principal` is the caller.
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

fun Route.adminOnly(build: UserRoutes): Route =
    authenticateWith(roleAuth, roles = setOf(UserRole.ADMIN), build = build).apply { install(AdminAudit) }

fun Route.optionallyAuthenticated(build: OptionalUserRoutes): Route =
    authenticateWithOptional(roleAuth, build = build).describe { security { optional() } }

/** Whether the caller is a logged-in, not banned admin. */
context(_: RolesContext<UserPrincipal, UserRole>, _: RoutingContext)
val UserPrincipal?.isAdmin: Boolean get() = this != null && UserRole.ADMIN in roles

/** Logs every admin change (anything but reads) as `authz_admin`, with who, what and the outcome. */
private val AdminAudit =
    createRouteScopedPlugin("AdminAudit") {
        on(ResponseSent) { call ->
            if (call.request.httpMethod == HttpMethod.Get || call.request.httpMethod == HttpMethod.Head) return@on
            audit(
                "authz_admin",
                "user" to call.principal<UserPrincipal>()?.id,
                "action" to "${call.request.httpMethod.value} ${call.request.path()}",
                "status" to call.response.status()?.value,
            )
        }
    }
