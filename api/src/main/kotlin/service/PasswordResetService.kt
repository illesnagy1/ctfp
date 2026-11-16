package com.ctfp.service

import com.ctfp.domain.dao.PasswordResetTokenDAO
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.PasswordResetTokenTable
import com.ctfp.domain.model.UserTable
import com.ctfp.service.helper.MailSender
import com.ctfp.service.helper.PasswordHasher
import com.ctfp.service.helper.validatePassword
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.jdbc.update
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Password recovery by email. The link carries a random token; only its SHA-256 is stored, so a leaked database
 * can't be used to reset passwords.
 */
class PasswordResetService(
    private val hasher: PasswordHasher,
    private val mail: MailSender,
    /** The frontend's origin; the link opens its `/reset-password` page. */
    private val publicUrl: String,
    /** Sends the emails, so a request returns before the SMTP server answers. */
    private val scope: CoroutineScope,
    private val ttl: Duration = 1.hours,
) {
    /**
     * Emails a reset link if [email] belongs to an account that isn't banned. Returns the same either way, and without
     * waiting for the email, so callers can't tell which addresses have accounts. The job is for tests.
     */
    suspend fun request(email: String): Job? {
        val token = ByteArray(TOKEN_BYTES).also(random::nextBytes).let(Base64.getUrlEncoder().withoutPadding()::encodeToString)
        val recipient =
            withTransaction {
                val now = Clock.System.now()
                val user =
                    UserDAO.find { UserTable.email eq email.trim().lowercase() }.firstOrNull()?.takeUnless { it.isBanned }
                        ?: return@withTransaction null
                // At most one email per account and cooldown, so the form can't be used to flood an inbox.
                val recent = (PasswordResetTokenTable.userId eq user.id) and (PasswordResetTokenTable.createdAt greater now - COOLDOWN)
                if (!PasswordResetTokenDAO.find(recent).empty()) return@withTransaction null

                PasswordResetTokenDAO.new {
                    this.user = user
                    this.token = sha256(token)
                    expiresAt = now + ttl
                }
                user.email to user.username
            } ?: return null

        val (address, username) = recipient
        val link = "${publicUrl.trimEnd('/')}/reset-password?token=$token"
        return scope.launch {
            runCatching { mail.send(address, SUBJECT, body(username, link)) }
                .onFailure { log.error("Sending the password reset email failed", it) }
        }
    }

    /** Sets a new password with a token from [request]; 400 if the token is unknown, used or expired. */
    suspend fun reset(
        token: String,
        password: String,
    ) {
        validatePassword(password)
        val passwordHash = hasher.hash(password)
        withTransaction {
            val now = Clock.System.now()
            // Locked, so two requests with the same link can't both use it.
            val reset =
                PasswordResetTokenDAO
                    .find { PasswordResetTokenTable.token eq sha256(token.trim()) }
                    .forUpdate()
                    .firstOrNull()
            if (reset == null || reset.usedAt != null || reset.expiresAt < now) {
                throw ApiException(HttpStatusCode.BadRequest, "This reset link is invalid or has expired")
            }
            reset.user.passwordHash = passwordHash
            // Using one link spends every other open link of the account too.
            PasswordResetTokenTable.update({
                (PasswordResetTokenTable.userId eq reset.user.id) and PasswordResetTokenTable.usedAt.isNull()
            }) { it[usedAt] = now }
        }
    }

    private fun body(
        username: String,
        link: String,
    ) = """
        |Someone asked to reset the password of $username.
        |
        |Open this link within ${ttl.inWholeMinutes} minutes to choose a new password:
        |$link
        |
        |If that wasn't you, ignore this email; your password stays the same.
        """.trimMargin()

    private companion object {
        const val TOKEN_BYTES = 32
        const val SUBJECT = "Reset your password"
        val COOLDOWN = 1.minutes
        val random = SecureRandom()
        val log = LoggerFactory.getLogger(PasswordResetService::class.java)

        fun sha256(token: String): String =
            MessageDigest.getInstance("SHA-256").digest(token.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}
