package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.operation
import com.ctfp.dto.UserUpdateRequest
import com.ctfp.plugins.authenticated
import com.ctfp.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.hide
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/** Public user views, plus the caller's own account under `/me`. Admin views live in [adminUser]. */
fun Route.user(service: UserService) {
    authenticated {
        route("/users") {
            get {
                call.respond(service.getAllUsers())
            }.operation("listUsers", Unauthorized)

            route("/me") {
                get {
                    call.respond(service.getAdminUser(call.principal.id))
                }.operation("getMe", Unauthorized, NotFound)
                put {
                    service.updateUser(call.principal.id, call.receive<UserUpdateRequest>())

                    call.respond(HttpStatusCode.NoContent)
                }.operation("updateMe", BadRequest, Unauthorized, NotFound, Conflict)
            }

            route("/{id}") {
                get {
                    call.respond(service.getUser(call.getId()))
                }.operation("getUser", BadRequest, Unauthorized, NotFound)

                route("submissions") {
                    get {
                    }.hide()
                    post {
                    }.hide()
                    put {
                    }.hide()
                    delete {
                    }.hide()
                }
            }
        }
    }
}
