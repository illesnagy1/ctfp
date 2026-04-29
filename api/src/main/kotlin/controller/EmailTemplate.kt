package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.Challenge
import com.ctfp.dto.EmailTemplate
import com.ctfp.repository.EmailTemplateRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.emailTemplate(repository: EmailTemplateRepository) {
    route("/email-template") {
        get {
            call.respond(repository.getAllEmailTemplates())
        }
        get("/{id}") {
            val id = call.getId()
            val emailTemplate = repository.getEmailTemplate(id)

            call.respond(emailTemplate)
        }
        post {
            val emailTemplate = call.receive<EmailTemplate>()
            repository.createEmailTemplate(emailTemplate)

            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.getId()
            val emailTemplate = call.receive<EmailTemplate>()
            repository.updateEmailTemplate(id, emailTemplate)

            call.respond(HttpStatusCode.NoContent)
        }
        delete("/{id}") {
            val id = call.getId()
            repository.deleteEmailTemplate(id)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
