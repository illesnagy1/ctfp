package com.ctfp.controller

import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.auth() {
    route("/auth") {
        post("login") {

        }
        post("register") {

        }
        post("logout") {

        }
    }
}