package com.ctfp.controller

import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.notification() {
    route("/notification") {
        get {

        }
        get("/{id}") {

        }
        post {

        }
        put("/{id}") {

        }
        delete("/{id}") {

        }
    }
}
