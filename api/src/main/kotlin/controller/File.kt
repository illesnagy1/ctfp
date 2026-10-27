package com.ctfp.controller

import com.ctfp.controller.helper.RequireAdmin
import com.ctfp.controller.helper.errors
import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.isAdmin
import com.ctfp.dto.FileResponse
import com.ctfp.service.ApiException
import com.ctfp.service.FileService
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.PayloadTooLarge
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.suppressCompression
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.file(service: FileService) {
    route("/challenges/{id}/files") {
        // Same visibility as the challenge itself.
        authenticate(optional = true) {
            get {
                call.respond(service.getChallengeFiles(call.getId(), includeUnreleased = call.isAdmin()))
            }.errors(BadRequest, NotFound)
        }
        authenticate {
            install(RequireAdmin)

            // multipart/form-data with one `file` part; responds with the stored file's metadata.
            post {
                val challengeId = call.getId()
                var uploaded: FileResponse? = null
                // Our own limit is enforced while storing; this one only stops Ktor from cutting the part short first.
                call.receiveMultipart(formFieldLimit = service.maxBytes + 1).forEachPart { part ->
                    try {
                        if (part is PartData.FileItem && part.name == "file" && uploaded == null) {
                            uploaded = service.uploadChallengeFile(
                                challengeId,
                                part.originalFileName,
                                part.contentType?.toString(),
                                part.provider(),
                            )
                        }
                    } finally {
                        part.dispose()
                    }
                }

                call.respond(HttpStatusCode.Created, uploaded ?: throw ApiException(BadRequest, "Missing `file` part"))
            }.errors(BadRequest, Unauthorized, Forbidden, NotFound, PayloadTooLarge)
        }
    }

    route("/files/{id}") {
        authenticate(optional = true) {
            get("/download") {
                val download = service.download(call.getId(), includeUnreleased = call.isAdmin())

                // Always a download, never rendered: an uploaded .html or .svg can't run script on our origin.
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment
                        .withParameter(ContentDisposition.Parameters.FileName, asciiFilename(download.filename))
                        .toString(),
                )
                call.response.header("X-Content-Type-Options", "nosniff")
                call.response.header("Content-Security-Policy", "sandbox")
                // Access depends on who's asking, so shared caches must not keep it.
                call.response.header(HttpHeaders.CacheControl, "private, no-cache")
                // Attachments are usually archives or binaries already; compressing them only costs CPU.
                call.suppressCompression()
                call.respond(download.content)
            }.errors(BadRequest, NotFound)
        }
        authenticate {
            install(RequireAdmin)

            delete {
                service.delete(call.getId())

                call.respond(HttpStatusCode.NoContent)
            }.errors(BadRequest, Unauthorized, Forbidden, NotFound)
        }
    }
}

/** Header values must be ASCII; the original name stays in the file's metadata. */
private fun asciiFilename(filename: String) =
    filename.map { if (it in ' '..'~' && it != '"' && it != '\\') it else '_' }.joinToString("")
