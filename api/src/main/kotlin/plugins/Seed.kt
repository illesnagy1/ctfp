package com.ctfp.plugins

import com.ctfp.config.enum.UserRole
import com.ctfp.config.uploadsConfig
import com.ctfp.domain.dao.RegistrationCodeDAO
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.AdminUserCreateRequest
import com.ctfp.dto.ChallengeRequest
import com.ctfp.dto.DecayFunction
import com.ctfp.dto.FlagRequest
import com.ctfp.service.ChallengeService
import com.ctfp.service.FlagService
import com.ctfp.service.SubmissionService
import com.ctfp.service.AdminUserService
import com.ctfp.service.helper.PasswordHasher
import com.ctfp.service.helper.sha256Hex
import com.ctfp.storage.LocalFileStorage
import io.ktor.server.application.Application
import io.ktor.server.application.log
import kotlin.io.path.Path
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/** Password of every seeded account. */
private const val SEED_PASSWORD = "password123"
private const val SEED_CODE_MAX_USES = 10

/**
 * Fills an empty database with sample users, challenges and solves, only in development mode
 * (`KTOR_DEVELOPMENT=true`, as `mise run dev:api` sets) and only when `seed` is on. Goes through the
 * services, so the data passes the same validation as the api.
 */
suspend fun Application.configureSeed() {
    if (!developmentMode ||
        !environment.config
            .property("seed")
            .getString()
            .toBoolean()
    ) {
        return
    }
    if (withTransaction { !UserDAO.all().empty() }) return

    val users = AdminUserService(PasswordHasher())
    val challenges = ChallengeService(LocalFileStorage(Path(environment.config.uploadsConfig().dir)))
    val flags = FlagService()
    val submissions = SubmissionService(challenges)

    fun user(
        name: String,
        role: UserRole = UserRole.USER,
        country: String? = null,
        isHidden: Boolean = false,
        isBanned: Boolean = false,
    ) = AdminUserCreateRequest(
        codeId = null,
        username = name,
        email = "$name@example.com",
        password = SEED_PASSWORD,
        language = null,
        country = country,
        website = null,
        role = role,
        isVerified = true,
        isHidden = isHidden,
        isBanned = isBanned,
        visibilitySettings = null,
    )
    users.createUser(user("admin", UserRole.ADMIN))
    val alice = users.createUser(user("alice", country = "HU"))
    val bob = users.createUser(user("bob", country = "DE"))
    val carol = users.createUser(user("carol", country = "US"))
    users.createUser(user("hank", isHidden = true))
    users.createUser(user("bella", isBanned = true))

    val now = Clock.System.now()

    suspend fun challenge(
        title: String,
        category: String,
        flag: FlagRequest,
        isVisible: Boolean = true,
        publishAt: kotlin.time.Instant = now - 1.days,
        maxAttempts: Int = 0,
    ): Int {
        val id =
            challenges.create(
                ChallengeRequest(title, "Sample **$category** challenge.", isVisible, publishAt, maxAttempts, listOf(category), 0),
            )
        flags.create(id, flag)
        return id
    }
    val warmup = challenge("Warmup", "misc", FlagRequest("ctfp\\{welcome\\}", isCaseSensitive = false, baseValue = 50))
    val sqli =
        challenge(
            "Login Bypass",
            "web",
            FlagRequest("ctfp\\{or_1=1\\}", baseValue = 300, decayValue = 50, minValue = 100, decayFun = DecayFunction.LINEAR),
        )
    val crypto =
        challenge(
            "Caesar's Secret",
            "crypto",
            FlagRequest("ctfp\\{rot13\\}", baseValue = 200, decayValue = 40, minValue = 50, decayFun = DecayFunction.LOGARITHMIC),
            maxAttempts = 5,
        )
    challenge("Draft", "pwn", FlagRequest("ctfp\\{draft\\}", baseValue = 500), isVisible = false)
    challenge("Coming Soon", "rev", FlagRequest("ctfp\\{later\\}", baseValue = 400), publishAt = now + 7.days)

    for ((player, solved) in listOf(alice to listOf(warmup, sqli, crypto), bob to listOf(warmup, sqli), carol to listOf(warmup))) {
        submissions.submit(player, crypto, "ctfp{wrong}", isAdmin = false)
        for (id in solved) {
            val flag = mapOf(warmup to "CTFP{Welcome}", sqli to "ctfp{or_1=1}", crypto to "ctfp{rot13}").getValue(id)
            submissions.submit(player, id, flag, isAdmin = false)
        }
    }

    withTransaction {
        RegistrationCodeDAO.new {
            code = sha256Hex("DEV-CODE")
            maxUses = SEED_CODE_MAX_USES
            expiresAt = now + 30.days
        }
    }

    log.info("Seeded development data: log in as admin : $SEED_PASSWORD")
}
