package com.ctfp.controller

import com.ctfp.service.AuthService
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.auth(service: AuthService) {
    route("/auth") {
        post("login") {

        }
        post("register") {

        }
        post("logout") {

        }
    }
}
