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
import com.ctfp.dto.Limits
import com.ctfp.dto.SubmissionResponse
import com.ctfp.dto.SubmissionResult
import com.ctfp.service.helper.findUser
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

class SubmissionService(
    private val challenges: ChallengeService,
) {
    /**
     * Checks [submitted] against the challenge's flags and records the guess. Hidden and unpublished challenges are a
     * 404 for non-admins, as when viewing them. Not yet enforced: the challenge's time limit, and team solves.
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
        challenges.get(challengeId, includeUnreleased = isAdmin)
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

    /** [submitted] trimmed; 400 if empty or too long. */
    private fun validFlag(submitted: String): String {
        val flag = submitted.trim()
        if (flag.isEmpty()) throw ApiException(HttpStatusCode.BadRequest, "Flag is required")
        if (flag.length > Limits.SUBMITTED_FLAG_MAX) throw ApiException(HttpStatusCode.BadRequest, "Flag is too long")
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
        // Patterns are checked when saved; a broken one just never matches.
        return runCatching { Regex(pattern, options).matches(submitted) }.getOrDefault(false)
    }
}
