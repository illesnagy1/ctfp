package com.ctfp.storage

import com.ctfp.service.ApiException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.server.http.content.LocalPathContent
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.util.UUID

/** Stores uploads as files named by random UUIDs in [dir]; original names never reach the file system. */
class LocalFileStorage(private val dir: Path) : FileStorage {
    override suspend fun save(content: ByteReadChannel, maxBytes: Long): StoredFile = withContext(Dispatchers.IO) {
        Files.createDirectories(dir)
        val key = UUID.randomUUID().toString()
        // Written under a temporary name, so a half-written upload is never served.
        val temp = dir.resolve("$key.part")
        val digest = MessageDigest.getInstance("SHA-256")
        var size = 0L

        try {
            content.toInputStream().use { input ->
                Files.newOutputStream(temp, StandardOpenOption.CREATE_NEW).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        size += read
                        if (size > maxBytes) {
                            throw ApiException(HttpStatusCode.PayloadTooLarge, "File is larger than $maxBytes bytes")
                        }
                        digest.update(buffer, 0, read)
                        output.write(buffer, 0, read)
                    }
                }
            }
            Files.move(temp, path(key), StandardCopyOption.ATOMIC_MOVE)
        } catch (e: Throwable) {
            Files.deleteIfExists(temp)
            throw e
        }

        StoredFile(key, size, digest.digest().joinToString("") { "%02x".format(it) })
    }

    override fun content(key: String, contentType: ContentType): OutgoingContent =
        LocalPathContent(path(key), contentType)

    override suspend fun delete(key: String) {
        withContext(Dispatchers.IO) { Files.deleteIfExists(path(key)) }
    }

    /** Keys come from the database, but only ever resolve UUIDs, so no key can point outside [dir]. */
    private fun path(key: String): Path {
        require(KEY.matches(key)) { "Invalid storage key" }
        return dir.resolve(key)
    }

    private companion object {
        val KEY = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")
    }
}
