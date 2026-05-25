package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.Challenge
import com.ctfp.service.ChallengeService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond


fun Route.challenge(service: ChallengeService) {
    route("/challenge") {
        get {
            call.respond(service.getAllChallenges())
        }
        get("/{id}") {
            val id = call.getId()
            val challenge = service.getChallenge(id)

            call.respond(challenge)
        }
        post {
            val challenge = call.receive<Challenge>()
            service.createChallenge(challenge)

            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.getId()
            val challenge = call.receive<Challenge>()
            service.updateChallenge(id, challenge)

            call.respond(HttpStatusCode.NoContent)
        }
        delete("/{id}") {
            val id = call.getId()
            service.deleteChallenge(id)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
