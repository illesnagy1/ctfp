package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.operation
import com.ctfp.dto.FlagRequest
import com.ctfp.plugins.adminOnly
import com.ctfp.service.FlagService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/** Flags are the answers, so all of this is admins only, reading included. */
fun Route.flag(service: FlagService) {
    adminOnly {
        route("/challenges/{id}/flags") {
            get {
                call.respond(service.getForChallenge(call.getId()))
            }.operation("listChallengeFlags", BadRequest, Unauthorized, Forbidden, NotFound)
            post {
                val flag = service.create(call.getId(), call.receive<FlagRequest>())

                call.respond(HttpStatusCode.Created, flag)
            }.operation("createFlag", BadRequest, Unauthorized, Forbidden, NotFound, Conflict)
        }

        route("/flags/{id}") {
            put {
                service.update(call.getId(), call.receive<FlagRequest>())

                call.respond(HttpStatusCode.NoContent)
            }.operation("updateFlag", BadRequest, Unauthorized, Forbidden, NotFound, Conflict)
            delete {
                service.delete(call.getId())

                call.respond(HttpStatusCode.NoContent)
            }.operation("deleteFlag", BadRequest, Unauthorized, Forbidden, NotFound)
        }
    }
}
