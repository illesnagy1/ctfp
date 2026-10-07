package com.ctfp.controller

import com.ctfp.controller.helper.errors
import com.ctfp.dto.LoginRequest
import com.ctfp.dto.RegisterRequest
import com.ctfp.plugins.userId
import com.ctfp.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.auth(service: AuthService) {
    route("/auth") {
        post("login") {
            val request = call.receive<LoginRequest>()
            call.respond(service.login(request))
        }.errors(BadRequest, Unauthorized, Forbidden)
        post("register") {
            val request = call.receive<RegisterRequest>()
            call.respond(HttpStatusCode.Created, service.register(request))
        }.errors(BadRequest, Conflict)
        authenticate {
            get("me") {
                call.respond(service.me(call.userId()))
            }.errors(Unauthorized)
        }
    }
}
