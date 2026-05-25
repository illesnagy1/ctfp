package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.Flag
import com.ctfp.repository.IFlagRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.flag(repository: IFlagRepository) {
    route("/challenge/{challengeId}/flag") {
        get {
            val challengeId = call.parameters["challengeId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid challengeId")
            call.respond(repository.getFlagsForChallenge(challengeId))
        }
        get("/{id}") {
            val id = call.getId()
            val challengeId = call.parameters["challengeId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid challengeId")
            val flag = repository.getFlagForChallenge(challengeId, id)

            call.respond(flag)
        }
        post {
            val challengeId = call.parameters["challengeId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid challengeId")
            val flag = call.receive<Flag>()
            repository.createFlag(challengeId, flag)

            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.getId()
            val flag = call.receive<Flag>()
            val challengeId = call.parameters["challengeId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid challengeId")
            repository.updateFlag(challengeId, id, flag)

            call.respond(HttpStatusCode.NoContent)
        }
        delete("/{id}") {
            val id = call.getId()
            repository.deleteFlag(id)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
