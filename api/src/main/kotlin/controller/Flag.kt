package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.FlagRequest
import com.ctfp.service.FlagService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.flag(service: FlagService) {
    route("/flags") {
        get {
            val challengeId = call.parameters["challengeId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid challengeId")
            call.respond(service.getFlagsForChallenge(challengeId))
        }
        post {
            val flag = call.receive<FlagRequest>()
            service.createFlag(flag)

            call.respond(HttpStatusCode.Created)
        }
        put {

        }
        delete {

        }
        route("/{id}") {
            get {
                val id = call.getId()
                val challengeId = call.parameters["challengeId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid challengeId")
                val flag = service.getFlagForChallenge(challengeId, id)

                call.respond(flag)
            }
            put {
                val id = call.getId()
                val flag = call.receive<FlagRequest>()
                val challengeId = call.parameters["challengeId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid challengeId")
                service.updateFlag(challengeId, id, flag)

                call.respond(HttpStatusCode.NoContent)
            }
            delete {
                val id = call.getId()
                service.deleteFlag(id)

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
