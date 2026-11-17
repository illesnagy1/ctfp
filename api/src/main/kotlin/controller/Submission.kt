package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.operation
import com.ctfp.dto.SubmitFlagRequest
import com.ctfp.plugins.adminOnly
import com.ctfp.plugins.authenticated
import com.ctfp.plugins.isAdmin
import com.ctfp.service.SubmissionService
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.principal
import io.ktor.server.plugins.origin
import io.ktor.server.request.receive
import io.ktor.server.request.userAgent
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.submission(service: SubmissionService) {
    authenticated {
        // Answers whether the guess was right; 403 when banned or out of attempts.
        post("/challenges/{id}/submissions") {
            val flag = call.receive<SubmitFlagRequest>().flag
            call.respond(
                service.submit(
                    call.principal.id,
                    call.getId(),
                    flag,
                    isAdmin = call.principal.isAdmin,
                    // The client's address via X-Forwarded-For, as the frontend proxies every call.
                    ipAddress = call.request.origin.remoteAddress,
                    userAgent = call.request.userAgent(),
                ),
            )
        }.operation("submitFlag", BadRequest, Unauthorized, Forbidden, NotFound)

        // The caller's own guesses, newest first.
        get("/users/me/submissions") {
            call.respond(service.list(call.principal.id))
        }.operation("listMySubmissions", Unauthorized, NotFound)

        // Ids of the challenges the caller has solved, e.g. to mark them in the list.
        get("/users/me/solves") {
            call.respond(service.solvedChallengeIds(call.principal.id))
        }.operation("listMySolves", Unauthorized)
    }

    adminOnly {
        // Everyone's guesses, newest first, with where they came from.
        get("/admin/submissions") {
            call.respond(service.list())
        }.operation("listSubmissions", Unauthorized, Forbidden)

        // One user's guesses, for their profile page.
        get("/admin/users/{id}/submissions") {
            call.respond(service.list(call.getId()))
        }.operation("listUserSubmissions", BadRequest, Unauthorized, Forbidden, NotFound)
    }
}
