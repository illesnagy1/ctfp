package com.ctfp.service

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.dao.FlagDAO
import com.ctfp.domain.dao.SubmissionDAO
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeTable
import com.ctfp.domain.model.FlagTable
import com.ctfp.domain.model.SubmissionTable
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.Limits
import com.ctfp.dto.SubmissionResponse
import com.ctfp.dto.SubmissionResult
import com.ctfp.service.helper.audit
import com.ctfp.service.helper.findUser
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.slf4j.event.Level
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SubmissionService(
    private val challenges: ChallengeService,
) {
    /**
     * Checks [submitted] against the challenge's flags and records the guess. Hidden and unpublished challenges are a
     * 404 for non-admins, as when viewing them; after the CTF ends, and outside a timed challenge's window, a 403.
     * Not yet enforced: team solves.
     * [ipAddress] and [userAgent] are kept with the guess, the user agent cut to [Limits.USER_AGENT_MAX].
     */
    suspend fun submit(
        userId: Int,
        challengeId: Int,
        submitted: String,
        isAdmin: Boolean,
        ipAddress: String? = null,
        userAgent: String? = null,
    ): SubmissionResult {
        val visible = challenges.get(challengeId, includeUnreleased = isAdmin, userId = userId)
        if (!isAdmin) ensureOpen(visible)
        val flag = validFlag(submitted)

        return withTransaction {
            val user = submitter(userId)
            val challenge = ChallengeDAO[challengeId]

            val mine = (SubmissionTable.userId eq userId) and (SubmissionTable.challengeId eq challengeId)
            if (!SubmissionDAO.find { mine and (SubmissionTable.isCorrect eq true) }.empty()) {
                return@withTransaction SubmissionResult(correct = true, alreadySolved = true)
            }

            val limit = challenge.maxAttempts.takeIf { it > 0 }
            val wrong = SubmissionDAO.find { mine and (SubmissionTable.isCorrect eq false) }.count().toInt()
            if (limit != null && wrong >= limit) throw ApiException(HttpStatusCode.Forbidden, "No attempts left")

            val matched = FlagDAO.find { FlagTable.challengeId eq challengeId }.firstOrNull { it.matches(flag) }
            SubmissionDAO.new {
                this.user = user
                this.challenge = challenge
                this.flag = matched
                submittedFlag = flag
                isCorrect = matched != null
                this.ipAddress = ipAddress?.take(Limits.IP_ADDRESS_MAX)
                this.userAgent = userAgent?.take(Limits.USER_AGENT_MAX)
            }

            SubmissionResult(
                correct = matched != null,
                attemptsLeft = if (matched == null && limit != null) limit - wrong - 1 else null,
            )
        }
    }

    /** 403 once the CTF is over, and for a timed challenge before the player starts it or once their time is up. */
    private fun ensureOpen(challenge: ChallengeResponse) {
        val now = Clock.System.now()
        val endsAt = challenges.settings().endsAt
        if (endsAt != null && now >= endsAt) throw ApiException(HttpStatusCode.Forbidden, "The CTF is over")
        if (challenge.timeLimit <= 0) return
        val startedAt = challenge.startedAt ?: throw ApiException(HttpStatusCode.Forbidden, "Start the challenge first")
        if (now > startedAt + challenge.timeLimit.seconds) throw ApiException(HttpStatusCode.Forbidden, "Time is up for this challenge")
    }

    /** [submitted] trimmed, since players paste with whitespace; 400 if that leaves nothing. */
    private fun validFlag(submitted: String): String {
        val flag = submitted.trim()
        if (flag.isEmpty()) throw ApiException(HttpStatusCode.BadRequest, "Flag is required")
        return flag
    }

    /**
     * The submitting user, row-locked until the transaction ends; call inside a transaction. The lock makes a player's
     * submissions run one at a time, so parallel ones can't both solve or both use the last attempt.
     */
    private fun submitter(userId: Int): UserDAO {
        val user =
            UserDAO.find { UserTable.id eq userId }.forUpdate().singleOrNull()
                ?: throw ApiException(HttpStatusCode.Unauthorized, "Session is no longer valid")
        if (user.isBanned) throw ApiException(HttpStatusCode.Forbidden, "Banned users can't submit flags")
        return user
    }

    /** Ids of the challenges [userId] has solved. */
    suspend fun solvedChallengeIds(userId: Int): List<Int> =
        withTransaction {
            SubmissionTable
                .select(SubmissionTable.challengeId)
                .where { (SubmissionTable.userId eq userId) and (SubmissionTable.isCorrect eq true) }
                .withDistinct()
                .map { it[SubmissionTable.challengeId].value }
        }

    /**
     * Recorded guesses, newest first: all of them, or only [userId]'s; 404 if that user doesn't exist.
     * shortcut: no paging, the whole list is sent; add limit/offset once events reach many thousands of guesses.
     */
    suspend fun list(userId: Int? = null): List<SubmissionResponse> =
        withTransaction {
            userId?.let(::findUser)
            (SubmissionTable innerJoin UserTable innerJoin ChallengeTable)
                .selectAll()
                .apply { userId?.let { where { SubmissionTable.userId eq it } } }
                .orderBy(SubmissionTable.submittedAt to SortOrder.DESC, SubmissionTable.id to SortOrder.DESC)
                .map {
                    SubmissionResponse(
                        id = it[SubmissionTable.id].value,
                        challengeId = it[ChallengeTable.id].value,
                        challengeTitle = it[ChallengeTable.title],
                        userId = it[UserTable.id].value,
                        username = it[UserTable.username],
                        submittedFlag = it[SubmissionTable.submittedFlag],
                        isCorrect = it[SubmissionTable.isCorrect],
                        submittedAt = it[SubmissionTable.submittedAt],
                        ipAddress = it[SubmissionTable.ipAddress],
                        userAgent = it[SubmissionTable.userAgent],
                    )
                }
        }

    private fun FlagDAO.matches(submitted: String): Boolean {
        val options = if (isCaseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE)
        // Patterns are checked when saved; a broken one just never matches, nor does one that backtracks too long.
        val input = Deadline(submitted, System.nanoTime() + MATCH_TIMEOUT.inWholeNanoseconds)
        return runCatching { Regex(pattern, options).matches(input) }
            .onFailure { if (it is MatchTimeout) audit("flag_pattern_timeout", "flag" to id.value, level = Level.WARN) }
            .getOrDefault(false)
    }

    private companion object {
        val MATCH_TIMEOUT = 100.milliseconds
    }
}

private class MatchTimeout : RuntimeException()

/** [text] that throws [MatchTimeout] once [deadline] (System.nanoTime) passes; the regex engine reads it char by char. */
private class Deadline(
    private val text: CharSequence,
    private val deadline: Long,
) : CharSequence {
    override val length get() = text.length

    override fun get(index: Int): Char {
        if (System.nanoTime() > deadline) throw MatchTimeout()
        return text[index]
    }

    override fun subSequence(
        startIndex: Int,
        endIndex: Int,
    ): CharSequence = Deadline(text.subSequence(startIndex, endIndex), deadline)

    override fun toString() = text.toString()
}
