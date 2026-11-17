package com.ctfp.controller

import com.ctfp.controller.helper.operation
import com.ctfp.dto.Settings
import com.ctfp.plugins.adminOnly
import com.ctfp.service.SettingsService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.settings(service: SettingsService) {
    get("/settings") {
        call.respond(service.getSettings())
    }.operation("getSettings")

    adminOnly {
        route("/admin/settings") {
            put {
                call.respond(service.updateSettings(call.receive<Settings>()))
            }.operation("updateSettings", BadRequest, Unauthorized, Forbidden)
        }
    }
}
