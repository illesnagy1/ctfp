package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.User
import com.ctfp.repository.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.user(repository: UserRepository) {
    route("/user") {
        get {
            call.respond(repository.getAllUsers())
        }
        get("/{id}") {
            val id = call.getId()
            val user = repository.getUser(id)

            call.respond(user)
        }
        post {
            val user = call.receive<User>()
            repository.createUser(user)

            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.getId()
            val user = call.receive<User>()
            repository.updateUser(id, user)

            call.respond(HttpStatusCode.NoContent)
        }
        delete("/{id}") {
            val id = call.getId()
            repository.deleteUser(id)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
