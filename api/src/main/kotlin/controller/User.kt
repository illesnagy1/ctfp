package com.ctfp.controller

import com.ctfp.controller.helper.errors
import com.ctfp.controller.helper.getId
import com.ctfp.dto.UserUpdateRequest
import com.ctfp.plugins.userId
import com.ctfp.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

/** Public user views, plus the caller's own account under `/me`. Admin views live in [adminUser]. */
fun Route.user(service: UserService) {
    authenticate {
        route("/users") {
            get {
                call.respond(service.getAllUsers())
            }.errors(Unauthorized)

            route("/me") {
                get {
                    call.respond(service.getAdminUser(call.userId()))
                }.errors(Unauthorized, NotFound)
                put {
                    service.updateUser(call.userId(), call.receive<UserUpdateRequest>())

                    call.respond(HttpStatusCode.NoContent)
                }.errors(BadRequest, Unauthorized, NotFound, Conflict)
            }

            route("/{id}") {
                get {
                    call.respond(service.getUser(call.getId()))
                }.errors(BadRequest, Unauthorized, NotFound)

                route("submissions") {
                    get {

                    }
                    post {

                    }
                    put {

                    }
                    delete {

                    }
                }
            }
        }
    }
}
