package com.ctfp.controller

import com.ctfp.controller.helper.errors
import com.ctfp.controller.helper.getId
import com.ctfp.dto.SubmitFlagRequest
import com.ctfp.plugins.authenticated
import com.ctfp.plugins.isAdmin
import com.ctfp.service.SubmissionService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.submission(service: SubmissionService) {
    authenticated {
        // Answers whether the guess was right; 403 when banned or out of attempts.
        post("/challenges/{id}/submissions") {
            val flag = call.receive<SubmitFlagRequest>().flag
            call.respond(service.submit(call.principal.id, call.getId(), flag, isAdmin = call.principal.isAdmin))
        }.errors(BadRequest, Unauthorized, Forbidden, NotFound)

        // Ids of the challenges the caller has solved, e.g. to mark them in the list.
        get("/users/me/solves") {
            call.respond(service.solvedChallengeIds(call.principal.id))
        }.errors(Unauthorized)
    }
}
