package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.operation
import com.ctfp.dto.HintRequest
import com.ctfp.plugins.adminOnly
import com.ctfp.service.HintService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.hide
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.hint(service: HintService) {
    // Admins only for now: players should only see hints they unlocked, which isn't built yet.
    adminOnly {
        route("/hints") {
            get {
                val challengeId = call.parameters["challengeId"]?.toIntOrNull() ?: throw BadRequestException("Invalid challengeId")
                call.respond(service.getHintsForChallenge(challengeId))
            }.operation("listHints", BadRequest, Unauthorized, Forbidden)
            post {
                val hint = call.receive<HintRequest>()
                service.createHint(hint)

                call.respond(HttpStatusCode.Created)
            }.operation("createHint", BadRequest, Unauthorized, Forbidden)
            put {
            }.hide()
            delete {
            }.hide()
            route("/{id}") {
                get {
                    val id = call.getId()
                    val hint = service.getHint(id)

                    call.respond(hint)
                }.operation("getHint", BadRequest, Unauthorized, Forbidden, NotFound)
                put {
                    val id = call.getId()
                    val hint = call.receive<HintRequest>()
                    service.updateHint(id, hint)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("updateHint", BadRequest, Unauthorized, Forbidden)
                delete {
                    val id = call.getId()
                    service.deleteHint(id)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("deleteHint", BadRequest, Unauthorized, Forbidden, NotFound)
            }
        }
    }
}
