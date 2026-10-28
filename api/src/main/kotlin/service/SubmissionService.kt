package com.ctfp.service

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.dao.FlagDAO
import com.ctfp.domain.dao.SubmissionDAO
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.FlagTable
import com.ctfp.domain.model.SubmissionTable
import com.ctfp.dto.SubmissionResult
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select

class SubmissionService(private val challenges: ChallengeService) {
    /**
     * Checks [submitted] against the challenge's flags and records the guess. Hidden and unpublished challenges are a
     * 404 for non-admins, as when viewing them. Not yet enforced: the challenge's time limit, and team solves.
     */
    suspend fun submit(userId: Int, challengeId: Int, submitted: String, isAdmin: Boolean): SubmissionResult {
        challenges.get(challengeId, includeUnreleased = isAdmin)
        val flag = submitted.trim()
        if (flag.isEmpty()) throw ApiException(HttpStatusCode.BadRequest, "Flag is required")
        if (flag.length > MAX_LENGTH) throw ApiException(HttpStatusCode.BadRequest, "Flag is too long")

        return withTransaction {
            val user = UserDAO.findById(userId) ?: throw ApiException(HttpStatusCode.Unauthorized, "Session is no longer valid")
            if (user.isBanned) throw ApiException(HttpStatusCode.Forbidden, "Banned users can't submit flags")
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
            }

            SubmissionResult(
                correct = matched != null,
                attemptsLeft = if (matched == null && limit != null) limit - wrong - 1 else null,
            )
        }
    }

    /** Ids of the challenges [userId] has solved. */
    suspend fun solvedChallengeIds(userId: Int): List<Int> = withTransaction {
        SubmissionTable.select(SubmissionTable.challengeId)
            .where { (SubmissionTable.userId eq userId) and (SubmissionTable.isCorrect eq true) }
            .withDistinct()
            .map { it[SubmissionTable.challengeId].value }
    }

    private fun FlagDAO.matches(submitted: String): Boolean {
        val options = if (isCaseSensitive) emptySet() else setOf(RegexOption.IGNORE_CASE)
        // Patterns are checked when saved; a broken one just never matches.
        return runCatching { Regex(pattern, options).matches(submitted) }.getOrDefault(false)
    }

    private companion object {
        const val MAX_LENGTH = 1024
    }
}
