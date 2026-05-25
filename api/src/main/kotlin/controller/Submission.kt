package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.Submission
import com.ctfp.repository.ISubmissionRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.submission(repository: ISubmissionRepository) {
    route("/submission") {
        get {
            call.respond(repository.getAllSubmissions())
        }
        get("/{id}") {
            val id = call.getId()
            val submission = repository.getSubmission(id)

            call.respond(submission)
        }
        post {
            val submission = call.receive<Submission>()
            repository.createSubmission(submission)

            call.respond(HttpStatusCode.Created)
        }
        delete("/{id}") {
            val id = call.getId()
            repository.deleteSubmission(id)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
