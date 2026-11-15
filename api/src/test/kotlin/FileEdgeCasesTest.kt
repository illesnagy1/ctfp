package com.ctfp

import com.ctfp.domain.dao.FileDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.FileResponse
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/** Uploads go to UPLOADS_DIR, set to build/test-uploads by build.gradle.kts. Names are unique per test (shared database). */
class FileEdgeCasesTest {
    private suspend fun ApplicationTestBuilder.upload(
        challengeId: Int,
        admin: String,
        disposition: String?,
        part: String = "file",
    ) = client.post("/v1/challenges/$challengeId/files") {
        bearerAuth(admin)
        setBody(
            MultiPartFormDataContent(
                formData {
                    append(
                        part,
                        byteArrayOf(1, 2, 3),
                        Headers.build { disposition?.let { append(HttpHeaders.ContentDisposition, it) } },
                    )
                },
            ),
        )
    }

    private suspend fun ApplicationTestBuilder.challenge(
        admin: String,
        title: String,
    ): Int {
        call(
            HttpMethod.Post,
            "/v1/challenges",
            admin,
            """{"title":"$title","description":null,"publishAt":"2026-01-01T00:00:00Z","maxAttempts":0,"categories":[],"timeLimit":0}""",
        )
        return challengeId(title)
    }

    @Test
    fun `file names keep only their base name`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("file-name-admin")
            val id = challenge(admin, "file-names")

            val names =
                mapOf(
                    "filename=\"C:\\\\tmp\\\\win.txt\"" to "win.txt",
                    "filename=\"  spaced.txt  \"" to "spaced.txt",
                    "filename=\"dir/\"" to "file",
                    "filename=\"árvíztűrő.txt\"" to "árvíztűrő.txt",
                )
            for ((disposition, expected) in names) {
                val response = upload(id, admin, disposition)
                assertEquals(HttpStatusCode.Created, response.status, disposition)
                val file = Json.decodeFromString<FileResponse>(response.bodyAsText())
                assertEquals(expected, file.filename, disposition)
                if (expected == "árvíztűrő.txt") {
                    // Header values must be ASCII; the stored name keeps the accents.
                    val download = call(HttpMethod.Get, "/v1/files/${file.id}/download")
                    assertEquals("attachment; filename=_rv_zt_r_.txt", download.headers[HttpHeaders.ContentDisposition])
                }
            }
        }

    @Test
    fun `uploads need a file part and an existing challenge`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("file-part-admin")
            val id = challenge(admin, "file-parts")

            assertEquals(HttpStatusCode.BadRequest, upload(id, admin, "filename=\"x.bin\"", part = "other").status)
            assertEquals(HttpStatusCode.NotFound, upload(Int.MAX_VALUE, admin, "filename=\"x.bin\"").status)
            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Get, "/v1/challenges/${Int.MAX_VALUE}/files").status)
            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Delete, "/v1/files/${Int.MAX_VALUE}", admin).status)
            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Get, "/v1/files/${Int.MAX_VALUE}/download").status)
        }

    @Test
    fun `files of owners without visibility rules are admin only`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("file-owner-admin")
            val player = register("file-owner-player")
            val id = challenge(admin, "file-owner")
            val file = Json.decodeFromString<FileResponse>(upload(id, admin, "filename=\"page.bin\"").bodyAsText())
            withTransaction { FileDAO[file.id].ownerType = "page" }

            assertEquals(HttpStatusCode.NotFound, call(HttpMethod.Get, "/v1/files/${file.id}/download", player).status)
            assertEquals(HttpStatusCode.OK, call(HttpMethod.Get, "/v1/files/${file.id}/download", admin).status)
        }
}
