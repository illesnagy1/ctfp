package com.ctfp.storage

import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteReadChannel

/** A stored upload: its storage key, size and SHA-256 (hex). */
data class StoredFile(val key: String, val size: Long, val sha256: String)

/**
 * Where uploaded file contents live; metadata stays in the `files` table. Local directory for now, but an S3 backed
 * implementation can replace it without touching the services.
 */
interface FileStorage {
    /**
     * Streams [content] into storage under a new random key. Throws 413 once it exceeds [maxBytes]; nothing is kept
     * then.
     */
    suspend fun save(content: ByteReadChannel, maxBytes: Long): StoredFile

    /** The stored bytes of [key] as a response body. */
    fun content(key: String, contentType: ContentType): OutgoingContent

    /** Removes [key]; missing keys are ignored. */
    suspend fun delete(key: String)
}
