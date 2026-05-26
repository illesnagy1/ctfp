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
import io.ktor.server.routing.put

fun Route.file(service: FileService) {
    route("/files") {
        get {
            call.respond(service.getAllFiles())
        }
        post {
            val file = call.receive<File>()
            service.createFile(file)

            call.respond(HttpStatusCode.Created)
        }
        put {

        }
        delete {

        }
        route("/{id}") {
            get {
                val id = call.getId()
                val file = service.getFile(id)

                call.respond(file)
            }
            put {

            }
            delete {
                val id = call.getId()
                service.deleteFile(id)

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
