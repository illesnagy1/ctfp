package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.File
import com.ctfp.repository.IFileRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.file(repository: IFileRepository) {
    route("/file") {
        get {
            call.respond(repository.getAllFiles())
        }
        get("/{id}") {
            val id = call.getId()
            val file = repository.getFile(id)

            call.respond(file)
        }
        post {
            val file = call.receive<File>()
            repository.createFile(file)

            call.respond(HttpStatusCode.Created)
        }
        delete("/{id}") {
            val id = call.getId()
            repository.deleteFile(id)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
