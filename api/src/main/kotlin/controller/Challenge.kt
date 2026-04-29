package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.Challenge
import com.ctfp.repository.ChallengeRepository
import com.ctfp.repository.FlagRepository
import com.ctfp.repository.HintRepository
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond
import kotlin.text.toIntOrNull


fun Route.challenge(repository: ChallengeRepository, flagRepository: FlagRepository, hintRepository: HintRepository) {
    route("/challenge") {
        get {
            call.respond(repository.getAllChallenges())
        }
        get("/{id}") {
            val id = call.getId()
            val challenge = repository.getChallenge(id)

            call.respond(challenge)
        }
        post {
            val challenge = call.receive<Challenge>()
            repository.createChallenge(challenge)

            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.getId()
            val challenge = call.receive<Challenge>()
            repository.updateChallenge(id, challenge)

            call.respond(HttpStatusCode.NoContent)
        }
        delete("/{id}") {
            val id = call.getId()
            repository.deleteChallenge(id)

            call.respond(HttpStatusCode.NoContent)
        }

        route("/{challengeId}/flag") {
            val challengeId = parameters["challengeId"]?.toIntOrNull() ?: throw BadRequestException("invalid id")
            get {
                call.respond(flagRepository.)
            }
            get("/{id}") {
                val id = call.getId()

            }
            post {

            }
            put("/{id}") {

            }
            delete("/{id}") {

            }
        }
    }
}
