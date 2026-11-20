package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.operation
import com.ctfp.dto.ChallengeRequest
import com.ctfp.plugins.adminOnly
import com.ctfp.plugins.authenticated
import com.ctfp.plugins.isAdmin
import com.ctfp.plugins.optionallyAuthenticated
import com.ctfp.service.ChallengeService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.principal
import io.ktor.server.auth.principalOrNull
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.hide
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.challenge(service: ChallengeService) {
    route("/challenges") {
        optionallyAuthenticated {
            // Login is optional for reading; admins also get hidden and not yet published challenges.
            get {
                call.respond(service.getAll(includeUnreleased = call.principalOrNull.isAdmin, userId = call.principalOrNull?.id))
            }.operation("listChallenges")
        }
        adminOnly {
            post {
                val challenge = call.receive<ChallengeRequest>()
                service.create(challenge)

                call.respond(HttpStatusCode.Created)
            }.operation("createChallenge", BadRequest, Unauthorized, Forbidden, Conflict)
            put {
                val challenges = call.receive<Map<Int, ChallengeRequest>>()
                service.updateMany(challenges)

                call.respond(HttpStatusCode.NoContent)
            }.operation("updateChallenges", BadRequest, Unauthorized, Forbidden, NotFound, Conflict)
            // Flags, hints, their unlocks and submissions are removed with the challenge (ON DELETE CASCADE).
            delete {
                val ids = call.receive<List<Int>>()
                service.deleteMany(ids)

                call.respond(HttpStatusCode.NoContent)
            }.operation("deleteChallenges", BadRequest, Unauthorized, Forbidden)
        }
        route("/{id}") {
            optionallyAuthenticated {
                get {
                    val id = call.getId()
                    val challenge = service.get(id, includeUnreleased = call.principalOrNull.isAdmin, userId = call.principalOrNull?.id)

                    call.respond(challenge)
                }.operation("getChallenge", BadRequest, NotFound)
            }
            authenticated {
                // Starts the caller's clock on a timed challenge; the description and files unlock.
                post("start") {
                    call.respond(service.start(call.getId(), call.principal.id))
                }.operation("startChallenge", BadRequest, Unauthorized, NotFound)
            }
            adminOnly {
                put {
                    val id = call.getId()
                    val challenge = call.receive<ChallengeRequest>()
                    service.update(id, challenge)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("updateChallenge", BadRequest, Unauthorized, Forbidden, NotFound, Conflict)
                delete {
                    val id = call.getId()
                    service.delete(id)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("deleteChallenge", BadRequest, Unauthorized, Forbidden, NotFound)
            }
        }
    }
}
