package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.Submission
import com.ctfp.service.SubmissionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.response.respond
import io.ktor.server.routing.put

fun Route.submission(service: SubmissionService) {
    route("/submissions") {
        get {
            call.respond(service.getAllSubmissions())
        }
        post {
            val submission = call.receive<Submission>()
            service.createSubmission(submission)

            call.respond(HttpStatusCode.Created)
        }
        put {

        }
        delete {

        }
        route("/{id}") {
            get {
                val id = call.getId()
                val submission = service.getSubmission(id)

                call.respond(submission)
            }
            put {

            }
            delete {
                val id = call.getId()
                service.deleteSubmission(id)

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
