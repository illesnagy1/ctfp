package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.UserRequest
import com.ctfp.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.user(service: UserService) {
    route("/users") {
        get {
            call.respond(service.getAllUsers())
        }
        post {
            val user = call.receive<UserRequest>()
            service.createUser(user)

            call.respond(HttpStatusCode.Created)
        }
        put {
            val users = call.receive<Map<Int, UserRequest>>()
            service.updateUsers(users)
            call.respond(HttpStatusCode.NoContent)
        }
        delete {

        }
        route("/{id}") {
            get {
                val id = call.getId()
                val user = service.getUser(id)

                call.respond(user)
            }
            put {
                val id = call.getId()
                val user = call.receive<UserRequest>()
                service.updateUser(id, user)

                call.respond(HttpStatusCode.NoContent)
            }
            delete {
                val id = call.getId()
                service.deleteUser(id)

                call.respond(HttpStatusCode.NoContent)
            }

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
