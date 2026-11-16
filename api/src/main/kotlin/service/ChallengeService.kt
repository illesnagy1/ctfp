package com.ctfp.service

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.dao.FlagDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeTable
import com.ctfp.domain.model.FileOwner
import com.ctfp.domain.model.FileTable
import com.ctfp.domain.model.FlagTable
import com.ctfp.domain.model.SubmissionTable
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.ChallengeRequest
import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto
import com.ctfp.service.ScoreboardService.Companion.rankedPlayers
import com.ctfp.service.ScoreboardService.Companion.value
import com.ctfp.storage.FileStorage
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.time.Clock

class ChallengeService(
    private val storage: FileStorage,
) : DefaultService<Int, ChallengeDAO, ChallengeRequest, ChallengeResponse>(
        dao = ChallengeDAO,
        table = ChallengeTable,
        apply = ChallengeDAO::apply,
        toDto = ChallengeDAO::toDto,
    ) {
    /** Challenges players may see: visible and already published. */
    private fun released(): Op<Boolean> = (ChallengeTable.isVisible eq true) and (ChallengeTable.publishAt lessEq Clock.System.now())

    /** All challenges for admins, only released ones for everyone else; [userId] adds the caller's progress. */
    suspend fun getAll(
        includeUnreleased: Boolean,
        userId: Int? = null,
    ): List<ChallengeResponse> =
        withTransaction {
            val challenges = if (includeUnreleased) ChallengeDAO.all().toList() else ChallengeDAO.find(released()).toList()
            withStats(challenges, userId)
        }

    /** Unreleased challenges are a 404 for non-admins, the same as missing ones, so their ids don't leak. */
    suspend fun get(
        id: Int,
        includeUnreleased: Boolean,
        userId: Int? = null,
    ): ChallengeResponse =
        withTransaction {
            val visible = if (includeUnreleased) ChallengeTable.id eq id else (ChallengeTable.id eq id) and released()
            val challenge = ChallengeDAO.find(visible).firstOrNull() ?: throw ApiException(HttpStatusCode.NotFound, "Not found")
            withStats(listOf(challenge), userId).single()
        }

    /**
     * [challenges] with their current value and solver count, counted as on the scoreboard, and for [userId] whether
     * they solved each one and how many attempts are left. Must run inside a transaction.
     */
    private fun withStats(
        challenges: List<ChallengeDAO>,
        userId: Int?,
    ): List<ChallengeResponse> {
        val ids = challenges.map { it.id.value }
        if (ids.isEmpty()) return emptyList()

        val solves =
            (SubmissionTable innerJoin UserTable)
                .select(SubmissionTable.challengeId, SubmissionTable.flagId)
                .where { (SubmissionTable.isCorrect eq true) and (SubmissionTable.challengeId inList ids) and rankedPlayers() }
                .map { it[SubmissionTable.challengeId].value to it[SubmissionTable.flagId]?.value }
        val solversPerChallenge = solves.groupingBy { it.first }.eachCount()
        val solversPerFlag = solves.mapNotNull { it.second }.groupingBy { it }.eachCount()
        val values =
            FlagDAO
                .find { FlagTable.challengeId inList ids }
                .groupBy { it.readValues[FlagTable.challengeId].value }
                .mapValues { (_, flags) -> flags.maxOf { it.value(solversPerFlag[it.id.value] ?: 0) } }

        // The caller's own guesses: (challenge id, correct).
        val mine =
            userId?.let {
                SubmissionTable
                    .select(SubmissionTable.challengeId, SubmissionTable.isCorrect)
                    .where { (SubmissionTable.userId eq it) and (SubmissionTable.challengeId inList ids) }
                    .map { row -> row[SubmissionTable.challengeId].value to row[SubmissionTable.isCorrect] }
                    .groupBy({ (challengeId) -> challengeId }, { (_, correct) -> correct })
            }

        return challenges.map { challenge ->
            val id = challenge.id.value
            val guesses = mine?.get(id).orEmpty()
            val solved = mine?.let { guesses.any { it } }
            challenge.toDto().copy(
                value = values[id] ?: 0,
                solves = solversPerChallenge[id] ?: 0,
                solved = solved,
                attemptsLeft =
                    challenge.maxAttempts
                        .takeIf { solved == false && it > 0 }
                        ?.let { limit -> (limit - guesses.count { !it }).coerceAtLeast(0) },
            )
        }
    }

    // Flags, hints and submissions go with the challenge through ON DELETE CASCADE. Files are polymorphic (no foreign
    // key), so their rows are removed here and their contents once the delete is committed.

    override suspend fun delete(id: Int) {
        val keys =
            withTransaction {
                // Challenge row first: its lock makes a concurrent upload wait, so its file row is seen below.
                ChallengeDAO[id].delete()
                deleteFileRows(listOf(id))
            }
        FileService.deleteStored(storage, keys)
    }

    override suspend fun deleteMany(ids: List<Int>): Int {
        val (deleted, keys) =
            withTransaction {
                // Challenge rows first, see delete().
                val deleted = ChallengeTable.deleteWhere { ChallengeTable.id inList ids }
                deleted to deleteFileRows(ids)
            }
        FileService.deleteStored(storage, keys)
        return deleted
    }

    /** Must run inside a transaction; returns the storage keys of the removed rows. */
    private fun deleteFileRows(challengeIds: List<Int>): List<String> {
        val owned = (FileTable.ownerType eq FileOwner.CHALLENGE) and (FileTable.ownerId inList challengeIds)
        val keys = FileTable.select(FileTable.storageKey).where(owned).map { it[FileTable.storageKey] }
        FileTable.deleteWhere { owned }
        return keys
    }

    fun getAllFlags(id: Int) = ChallengeDAO[id].flags

    fun getAllSubmissions(id: Int) = ChallengeDAO[id].submissions

    fun getAllHints(id: Int) = ChallengeDAO[id].hints
}
