package com.ctfp.controller

import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.submission() {
    route("/submission") {
        get {

        }
        get("/{id}") {

        }
        post {

        }
        delete("/{id}") {

        }
    }
}
