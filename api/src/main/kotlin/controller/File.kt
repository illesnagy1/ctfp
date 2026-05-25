package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.File
import com.ctfp.service.FileService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.file(service: FileService) {
    route("/file") {
        get {
            call.respond(service.getAllFiles())
        }
        get("/{id}") {
            val id = call.getId()
            val file = service.getFile(id)

            call.respond(file)
        }
        post {
            val file = call.receive<File>()
            service.createFile(file)

            call.respond(HttpStatusCode.Created)
        }
        delete("/{id}") {
            val id = call.getId()
            service.deleteFile(id)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
