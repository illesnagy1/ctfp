package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.Hint
import com.ctfp.repository.HintRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.hint(repository: HintRepository) {
    route("/challenge/{challengeId}/hint") {
        get {
            val challengeId = call.parameters["challengeId"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid challengeId")
            call.respond(repository.getHintsForChallenge(challengeId))
        }
        get("/{id}") {
            val id = call.getId()
            val hint = repository.getHint(id)

            call.respond(hint)
        }
        post {
            val hint = call.receive<Hint>()
            repository.createHint(hint)

            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.getId()
            val hint = call.receive<Hint>()
            repository.updateHint(id, hint)

            call.respond(HttpStatusCode.NoContent)
        }
        delete("/{id}") {
            val id = call.getId()
            repository.deleteHint(id)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
