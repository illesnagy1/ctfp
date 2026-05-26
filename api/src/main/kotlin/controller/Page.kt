package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.Page
import com.ctfp.service.PageService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.page(service: PageService) {
    route("/pages") {
        get {
            call.respond(service.getAllPages())
        }
        post {
            val page = call.receive<Page>()
            service.createPage(page)

            call.respond(HttpStatusCode.Created)
        }
        put {

        }
        delete {

        }
        route("/{id}") {
            get {
                val id = call.getId()
                val page = service.getPage(id)

                call.respond(page)
            }
            put {
                val id = call.getId()
                val page = call.receive<Page>()
                service.updatePage(id, page)

                call.respond(HttpStatusCode.NoContent)
            }
            delete {
                val id = call.getId()
                service.deletePage(id)

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
