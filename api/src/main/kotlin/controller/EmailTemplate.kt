package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.operation
import com.ctfp.dto.EmailTemplateRequest
import com.ctfp.plugins.adminOnly
import com.ctfp.service.EmailTemplateService
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
import io.ktor.server.routing.openapi.hide
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.emailTemplate(service: EmailTemplateService) {
    // Admins only: templates are internal.
    adminOnly {
        route("/email-templates") {
            get {
                call.respond(service.getAllEmailTemplates())
            }.operation("listEmailTemplates", Unauthorized, Forbidden)
            post {
                val emailTemplate = call.receive<EmailTemplateRequest>()
                service.createEmailTemplate(emailTemplate)

                call.respond(HttpStatusCode.Created)
            }.operation("createEmailTemplate", BadRequest, Unauthorized, Forbidden, Conflict)
            route("/{id}") {
                get {
                    val id = call.getId()
                    val emailTemplate = service.getEmailTemplate(id)

                    call.respond(emailTemplate)
                }.operation("getEmailTemplate", BadRequest, Unauthorized, Forbidden, NotFound)
                put {
                    val id = call.getId()
                    val emailTemplate = call.receive<EmailTemplateRequest>()
                    service.updateEmailTemplate(id, emailTemplate)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("updateEmailTemplate", BadRequest, Unauthorized, Forbidden, Conflict)
                delete {
                    val id = call.getId()
                    service.deleteEmailTemplate(id)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("deleteEmailTemplate", BadRequest, Unauthorized, Forbidden, NotFound)
            }
        }
    }
}
