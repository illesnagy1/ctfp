package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.ChallengeRequest
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
    route("/challenges") {
        get {
            call.respond(service.getAll())
        }
        post {
            val challenge = call.receive<ChallengeRequest>()
            service.create(challenge)

            call.respond(HttpStatusCode.Created)
        }
        put {
            val challenges = call.receive<Map<Int, ChallengeRequest>>()
            service.updateMany(challenges)

            call.respond(HttpStatusCode.NoContent)
        }
        delete {
            val ids = call.receive<List<Int>>()
            service.deleteMany(ids)

            call.respond(HttpStatusCode.NoContent)
        }
        route("/{id}") {
            get {
                val id = call.getId()
                val challenge = service.get(id)

                call.respond(challenge)
            }
            put {
                val id = call.getId()
                val challenge = call.receive<ChallengeRequest>()
                service.update(id, challenge)

                call.respond(HttpStatusCode.NoContent)
            }
            delete {
                val id = call.getId()
                service.delete(id)

                call.respond(HttpStatusCode.NoContent)
            }

            route("flags") {
                get {

                }
                post {

                }
                put {

                }
                delete {

                }
            }

            route("hints") {
                get {

                }
                post {

                }
                put {

                }
                delete {

                }
            }

            route("submissions") {
                get {

                }
                post {

                }
                put {

                }
                delete {

                }
            }
        }
    }
}
