package com.ctfp.service

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeTable
import com.ctfp.domain.model.FileOwner
import com.ctfp.domain.model.FileTable
import com.ctfp.dto.ChallengeRequest
import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto
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


class ChallengeService(private val storage: FileStorage) : DefaultService<Int, ChallengeDAO, ChallengeRequest, ChallengeResponse>(
    dao = ChallengeDAO,
    table = ChallengeTable,
    apply = ChallengeDAO::apply,
    toDto = ChallengeDAO::toDto
) {
    /** Challenges players may see: visible and already published. */
    private fun released(): Op<Boolean> =
        (ChallengeTable.isVisible eq true) and (ChallengeTable.publishAt lessEq Clock.System.now())

    /** All challenges for admins, only released ones for everyone else. */
    suspend fun getAll(includeUnreleased: Boolean): List<ChallengeResponse> =
        if (includeUnreleased) getAll() else withTransaction { ChallengeDAO.find(released()).map { it.toDto() } }

    /** Unreleased challenges are a 404 for non-admins, the same as missing ones, so their ids don't leak. */
    suspend fun get(id: Int, includeUnreleased: Boolean): ChallengeResponse =
        if (includeUnreleased) get(id) else withTransaction {
            ChallengeDAO.find { (ChallengeTable.id eq id) and released() }.firstOrNull()?.toDto()
                ?: throw ApiException(HttpStatusCode.NotFound, "Not found")
        }

    // Flags, hints and submissions go with the challenge through ON DELETE CASCADE. Files are polymorphic (no foreign
    // key), so their rows are removed here and their contents once the delete is committed.

    override suspend fun delete(id: Int) {
        val keys = withTransaction {
            val challenge = ChallengeDAO[id]
            deleteFileRows(listOf(id)).also { challenge.delete() }
        }
        FileService.deleteStored(storage, keys)
    }

    override suspend fun deleteMany(ids: List<Int>): Int {
        val (deleted, keys) = withTransaction {
            val keys = deleteFileRows(ids)
            ChallengeTable.deleteWhere { ChallengeTable.id inList ids } to keys
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
