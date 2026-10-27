package com.ctfp.service

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.dao.FileDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.FileOwner
import com.ctfp.domain.model.FileTable
import com.ctfp.dto.FileResponse
import com.ctfp.dto.toDto
import com.ctfp.storage.FileStorage
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteReadChannel
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.slf4j.LoggerFactory

/** A file ready to stream: its display name and contents. */
class FileDownload(val filename: String, val content: OutgoingContent)

class FileService(
    private val storage: FileStorage,
    private val challenges: ChallengeService,
    val maxBytes: Long,
) {
    /** A challenge's files, visible exactly when the challenge is (see [ChallengeService.get]). */
    suspend fun getChallengeFiles(challengeId: Int, includeUnreleased: Boolean): List<FileResponse> {
        challenges.get(challengeId, includeUnreleased)
        return withTransaction {
            FileDAO.find { ownedBy(FileOwner.CHALLENGE, challengeId) }.sortedBy { it.id.value }.map { it.toDto() }
        }
    }

    /** Stores [content] as a file of the challenge; 404 if the challenge doesn't exist. */
    suspend fun uploadChallengeFile(
        challengeId: Int,
        filename: String?,
        mimeType: String?,
        content: ByteReadChannel,
    ): FileResponse {
        // Checked before storing too, so a typo'd id doesn't cost a whole upload.
        challenges.get(challengeId)
        val stored = storage.save(content, maxBytes)

        try {
            return withTransaction {
                // Again in this transaction, in case the challenge was deleted during the upload.
                ChallengeDAO[challengeId]
                FileDAO.new {
                    ownerType = FileOwner.CHALLENGE
                    ownerId = challengeId
                    this.filename = displayName(filename)
                    storageKey = stored.key
                    size = stored.size
                    this.mimeType = mimeType?.take(128)
                    sha256 = stored.sha256
                }.toDto()
            }
        } catch (e: Throwable) {
            storage.delete(stored.key)
            throw e
        }
    }

    /**
     * The file's contents if its owner is visible to the caller; otherwise the same 404 as a missing file. Always
     * served as `application/octet-stream`, whatever was uploaded, so browsers download rather than render it.
     */
    suspend fun download(id: Int, includeUnreleased: Boolean): FileDownload {
        val file = withTransaction { FileDAO.findById(id) } ?: throw notFound()
        when (file.ownerType) {
            FileOwner.CHALLENGE -> challenges.get(file.ownerId, includeUnreleased)
            // Owners without visibility rules yet are admin only.
            else -> if (!includeUnreleased) throw notFound()
        }
        return FileDownload(file.filename, storage.content(file.storageKey, ContentType.Application.OctetStream))
    }

    suspend fun delete(id: Int) {
        val key = withTransaction {
            val file = FileDAO.findById(id) ?: throw notFound()
            file.storageKey.also { file.delete() }
        }
        deleteStored(storage, listOf(key))
    }

    private fun notFound() = ApiException(HttpStatusCode.NotFound, "Not found")

    companion object {
        private val log = LoggerFactory.getLogger(FileService::class.java)

        fun ownedBy(ownerType: String, ownerId: Int) =
            (FileTable.ownerType eq ownerType) and (FileTable.ownerId eq ownerId)

        /** Only the base name, without any directories a client put in it; it's never used as a path. */
        fun displayName(filename: String?): String =
            filename?.substringAfterLast('/')?.substringAfterLast('\\')?.trim()?.take(255)?.ifEmpty { null } ?: "file"

        /**
         * Removes stored contents after their rows are gone. A failure only leaves an orphaned file behind, so it's
         * logged rather than undoing the delete.
         */
        suspend fun deleteStored(storage: FileStorage, keys: List<String>) {
            for (key in keys) {
                runCatching { storage.delete(key) }.onFailure { log.warn("Couldn't delete stored file $key", it) }
            }
        }
    }
}
