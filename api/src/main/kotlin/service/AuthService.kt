package com.ctfp.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ctfp.config.JwtConfig
import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.RegistrationCodeDAO
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.RegistrationCodeTable
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.AuthResponse
import com.ctfp.dto.AuthUser
import com.ctfp.dto.LoginRequest
import com.ctfp.dto.RegisterRequest
import com.ctfp.dto.toAuthUser
import com.ctfp.service.helper.PasswordHasher
import com.ctfp.service.helper.audit
import com.ctfp.service.helper.ensureAvailable
import com.ctfp.service.helper.sha256Hex
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.slf4j.event.Level
import kotlin.time.Clock
import java.util.Date

class AuthService(
    private val jwt: JwtConfig,
    private val hasher: PasswordHasher,
    private val settings: SettingsService,
) {
    private val algorithm = Algorithm.HMAC256(jwt.secret)

    /** Verified against when the user doesn't exist, so response time doesn't reveal which usernames are taken. */
    private var dummyHash: String? = null

    suspend fun register(request: RegisterRequest): AuthResponse {
        if (!settings.getSettings().registrationOpen) throw ApiException(HttpStatusCode.Forbidden, "Registration is closed")

        val username = request.username
        val email = request.email.lowercase()

        val passwordHash = hasher.hash(request.password)

        val user =
            withTransaction {
                ensureAvailable(request.username, request.email)

                val code = request.registrationCode?.takeIf { it.isNotBlank() }?.let { findUsableCode(it.trim()) }

                UserDAO
                    .new {
                        this.username = username
                        this.email = email
                        this.passwordHash = passwordHash
                        this.role = UserRole.USER
                        this.registrationCode = code
                    }.toAuthUser()
            }
        audit("user_created", "user" to user.id, "username" to user.username, "code" to request.registrationCode?.isNotBlank())

        return issue(user)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        val login = request.login.trim()
        val user =
            withTransaction {
                UserDAO
                    .find { (UserTable.username eq login) or (UserTable.email eq login.lowercase()) }
                    .firstOrNull()
                    ?.let { Triple(it.toAuthUser(), it.passwordHash, it.isBanned) }
            }

        if (user == null) {
            hasher.verify(request.password, dummyHash ?: hasher.hash("dummy").also { dummyHash = it })
            audit("authn_login_fail", "login" to login, "reason" to "unknown user", level = Level.WARN)
            throw ApiException(HttpStatusCode.Unauthorized, "Invalid username or password")
        }

        val (authUser, passwordHash, isBanned) = user
        if (!hasher.verify(request.password, passwordHash)) {
            audit("authn_login_fail", "user" to authUser.id, "reason" to "wrong password", level = Level.WARN)
            throw ApiException(HttpStatusCode.Unauthorized, "Invalid username or password")
        }
        if (isBanned) {
            audit("authn_login_fail", "user" to authUser.id, "reason" to "banned", level = Level.WARN)
            throw ApiException(HttpStatusCode.Forbidden, "This account is banned")
        }
        // Hashes made before the work factor was raised are upgraded while the password is at hand.
        if (hasher.needsRehash(passwordHash)) {
            val upgraded = hasher.hash(request.password)
            withTransaction { UserDAO[authUser.id].passwordHash = upgraded }
        }

        audit("authn_login_success", "user" to authUser.id)
        return issue(authUser)
    }

    /** Current user for a valid token; 401 if the user was deleted or banned since the token was issued. */
    suspend fun me(userId: Int): AuthUser =
        withTransaction {
            val user = UserDAO.findById(userId)
            if (user == null || user.isBanned) throw ApiException(HttpStatusCode.Unauthorized, "Session is no longer valid")
            user.toAuthUser()
        }

    private fun issue(user: AuthUser): AuthResponse {
        val now = Clock.System.now()
        val expiresAt = now + jwt.ttl
        val token =
            JWT
                .create()
                .withIssuer(jwt.issuer)
                .withAudience(jwt.audience)
                // Only who it is: names and roles are looked up per request, so they can't go stale (RFC 8725 §3.10).
                .withSubject(user.id.toString())
                .withIssuedAt(Date(now.toEpochMilliseconds()))
                .withExpiresAt(Date(expiresAt.toEpochMilliseconds()))
                .sign(algorithm)

        return AuthResponse(token = token, expiresAt = expiresAt.epochSeconds, user = user)
    }

    private fun findUsableCode(code: String): RegistrationCodeDAO {
        // Locked, so parallel registrations count each other's uses instead of all passing the maxUses check.
        val registrationCode = RegistrationCodeDAO.find { RegistrationCodeTable.code eq sha256Hex(code) }.forUpdate().firstOrNull()
        if (
            registrationCode == null ||
            registrationCode.expiresAt < Clock.System.now() ||
            registrationCode.usedByUsers.count() >= registrationCode.maxUses
        ) {
            throw ApiException(HttpStatusCode.BadRequest, "Invalid registration code")
        }
        return registrationCode
    }
}
