package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.operation
import com.ctfp.dto.PageRequest
import com.ctfp.plugins.adminOnly
import com.ctfp.service.PageService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.hide
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.page(service: PageService) {
    // Admins only for now: nothing here hides hidden or protected pages from players.
    adminOnly {
        route("/pages") {
            get {
                call.respond(service.getAllPages())
            }.operation("listPages", Unauthorized, Forbidden)
            post {
                val page = call.receive<PageRequest>()
                service.createPage(page)

                call.respond(HttpStatusCode.Created)
            }.operation("createPage", BadRequest, Unauthorized, Forbidden, Conflict)
            route("/{id}") {
                get {
                    val id = call.getId()
                    val page = service.getPage(id)

                    call.respond(page)
                }.operation("getPage", BadRequest, Unauthorized, Forbidden, NotFound)
                put {
                    val id = call.getId()
                    val page = call.receive<PageRequest>()
                    service.updatePage(id, page)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("updatePage", BadRequest, Unauthorized, Forbidden, Conflict)
                delete {
                    val id = call.getId()
                    service.deletePage(id)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("deletePage", BadRequest, Unauthorized, Forbidden, NotFound)
            }
        }
    }
}
