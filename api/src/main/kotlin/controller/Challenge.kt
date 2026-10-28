package com.ctfp.controller

import com.ctfp.controller.helper.RequireAdmin
import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.isAdmin
import com.ctfp.dto.ChallengeRequest
import com.ctfp.service.ChallengeService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
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
        // Login is optional for reading; admins also get hidden and not yet published challenges.
        authenticate(optional = true) {
            get {
                call.respond(service.getAll(includeUnreleased = call.isAdmin()))
            }
        }
        authenticate {
            install(RequireAdmin)

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
            // Flags, hints, their unlocks and submissions are removed with the challenge (ON DELETE CASCADE).
            delete {
                val ids = call.receive<List<Int>>()
                service.deleteMany(ids)

                call.respond(HttpStatusCode.NoContent)
            }
        }
        route("/{id}") {
            authenticate(optional = true) {
                get {
                    val id = call.getId()
                    val challenge = service.get(id, includeUnreleased = call.isAdmin())

                    call.respond(challenge)
                }
            }
            authenticate {
                install(RequireAdmin)

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
        }
    }
}
