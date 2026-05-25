package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.Challenge
import com.ctfp.repository.IChallengeRepository
import com.ctfp.repository.IFlagRepository
import com.ctfp.repository.IHintRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond


fun Route.challenge(repository: IChallengeRepository, flagRepository: IFlagRepository, hintRepository: IHintRepository) {
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
    }
}
