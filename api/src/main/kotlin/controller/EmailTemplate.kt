package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.EmailTemplateRequest
import com.ctfp.service.EmailTemplateService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.emailTemplate(service: EmailTemplateService) {
    route("/email-templates") {
        get {
            call.respond(service.getAllEmailTemplates())
        }
        post {
            val emailTemplate = call.receive<EmailTemplateRequest>()
            service.createEmailTemplate(emailTemplate)

            call.respond(HttpStatusCode.Created)
        }
        put {

        }
        delete {

        }
        route("/{id}") {
            get {
                val id = call.getId()
                val emailTemplate = service.getEmailTemplate(id)

                call.respond(emailTemplate)
            }
            put {
                val id = call.getId()
                val emailTemplate = call.receive<EmailTemplateRequest>()
                service.updateEmailTemplate(id, emailTemplate)

                call.respond(HttpStatusCode.NoContent)
            }
            delete {
                val id = call.getId()
                service.deleteEmailTemplate(id)

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
