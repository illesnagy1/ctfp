package com.ctfp

import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.FileTable
import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.FileResponse
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Uploads go to UPLOADS_DIR, set to build/test-uploads with a 1 MiB limit by build.gradle.kts. */
class FileRoutesTest {
    private val uploads = Path.of(System.getenv("UPLOADS_DIR"))

    private suspend fun ApplicationTestBuilder.createChallenge(
        admin: String,
        title: String,
        isVisible: Boolean = true,
    ): Int {
        client.post("/v1/challenges") {
            bearerAuth(admin)
            contentType(ContentType.Application.Json)
            setBody(
                """{"title":"$title","description":null,"isVisible":$isVisible,""" +
                    """"publishAt":"2026-01-01T00:00:00Z","maxAttempts":0,"categories":[],"timeLimit":0}""",
            )
        }
        val all = client.get("/v1/challenges") { bearerAuth(admin) }.bodyAsText()
        return Json.decodeFromString<List<ChallengeResponse>>(all).single { it.title == title }.id
    }

    private suspend fun ApplicationTestBuilder.upload(
        challengeId: Int,
        token: String?,
        bytes: ByteArray,
        name: String,
    ) = client.post("/v1/challenges/$challengeId/files") {
        token?.let { bearerAuth(it) }
        setBody(
            MultiPartFormDataContent(
                formData {
                    append(
                        "file",
                        bytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, "text/html")
                            append(HttpHeaders.ContentDisposition, "filename=\"$name\"")
                        },
                    )
                },
            ),
        )
    }

    private suspend fun storageKey(fileId: Int) =
        withTransaction { FileTable.selectAll().where { FileTable.id eq fileId }.single()[FileTable.storageKey] }

    private fun sha256(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    @Test
    fun `admins upload, everyone downloads files of released challenges`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("file-admin")
            val player = register("file-player")
            val challengeId = createChallenge(admin, "with-files")
            val bytes = "<script>alert(1)</script>".toByteArray()

            assertEquals(HttpStatusCode.Unauthorized, upload(challengeId, null, bytes, "x.html").status)
            assertEquals(HttpStatusCode.Forbidden, upload(challengeId, player, bytes, "x.html").status)

            // The directory part of the name is dropped; it's never used as a path anyway.
            val response = upload(challengeId, admin, bytes, "../../evil.html")
            assertEquals(HttpStatusCode.Created, response.status)
            val file = Json.decodeFromString<FileResponse>(response.bodyAsText())
            assertEquals("evil.html", file.filename)
            assertEquals(bytes.size.toLong(), file.size)
            assertEquals(sha256(bytes), file.sha256)
            assertTrue(Files.exists(uploads.resolve(storageKey(file.id))))

            val listed = client.get("/v1/challenges/$challengeId/files").bodyAsText()
            assertEquals(listOf(file.id), Json.decodeFromString<List<FileResponse>>(listed).map { it.id })

            val download = client.get("/v1/files/${file.id}/download")
            assertEquals(HttpStatusCode.OK, download.status)
            assertContentEquals(bytes, download.bodyAsBytes())
            assertEquals("attachment; filename=evil.html", download.headers[HttpHeaders.ContentDisposition])
            assertEquals(ContentType.Application.OctetStream, download.contentType())
            assertEquals("nosniff", download.headers["X-Content-Type-Options"])

            assertEquals(HttpStatusCode.Forbidden, client.delete("/v1/files/${file.id}") { bearerAuth(player) }.status)
            val key = storageKey(file.id)
            assertEquals(HttpStatusCode.NoContent, client.delete("/v1/files/${file.id}") { bearerAuth(admin) }.status)
            assertFalse(Files.exists(uploads.resolve(key)))
            assertEquals(HttpStatusCode.NotFound, client.get("/v1/files/${file.id}/download").status)
        }

    @Test
    fun `files of hidden challenges are only listed and served to admins`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("hidden-file-admin")
            val challengeId = createChallenge(admin, "hidden-with-files", isVisible = false)
            val file = Json.decodeFromString<FileResponse>(upload(challengeId, admin, byteArrayOf(1, 2, 3), "a.bin").bodyAsText())

            assertEquals(HttpStatusCode.NotFound, client.get("/v1/challenges/$challengeId/files").status)
            assertEquals(HttpStatusCode.NotFound, client.get("/v1/files/${file.id}/download").status)
            assertEquals(HttpStatusCode.OK, client.get("/v1/challenges/$challengeId/files") { bearerAuth(admin) }.status)
            assertEquals(HttpStatusCode.OK, client.get("/v1/files/${file.id}/download") { bearerAuth(admin) }.status)
        }

    @Test
    fun `uploads over the limit are refused and leave nothing behind`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("big-file-admin")
            val challengeId = createChallenge(admin, "too-big")
            val before = Files.list(uploads).use { it.count() }

            val response = upload(challengeId, admin, ByteArray(1024 * 1024 + 1), "big.bin")
            assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
            assertEquals(before, Files.list(uploads).use { it.count() })
            assertEquals("[]", client.get("/v1/challenges/$challengeId/files") { bearerAuth(admin) }.bodyAsText())
        }

    @Test
    fun `deleting a challenge deletes its files`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("cascade-file-admin")
            val challengeId = createChallenge(admin, "files-cascade")
            val file = Json.decodeFromString<FileResponse>(upload(challengeId, admin, byteArrayOf(4, 5, 6), "b.bin").bodyAsText())
            val key = storageKey(file.id)

            assertEquals(HttpStatusCode.NoContent, client.delete("/v1/challenges/$challengeId") { bearerAuth(admin) }.status)
            assertFalse(Files.exists(uploads.resolve(key)))
            assertEquals(0L, withTransaction { FileTable.selectAll().where { FileTable.id eq file.id }.count() })
        }
}
