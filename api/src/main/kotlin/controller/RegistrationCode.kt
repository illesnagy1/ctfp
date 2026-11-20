package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.operation
import com.ctfp.dto.RegistrationCodeRequest
import com.ctfp.plugins.adminOnly
import com.ctfp.service.RegistrationCodeService
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

fun Route.registrationCode(repository: RegistrationCodeService) {
    // Admins only: the codes are secrets.
    adminOnly {
        route("/registration-codes") {
            get {
                call.respond(repository.getAllRegistrationCodes())
            }.operation("listRegistrationCodes", Unauthorized, Forbidden)
            post {
                val code = call.receive<RegistrationCodeRequest>()
                repository.createRegistrationCode(code)

                call.respond(HttpStatusCode.Created)
            }.operation("createRegistrationCode", BadRequest, Unauthorized, Forbidden, Conflict)
            route("/{id}") {
                get {
                    val id = call.getId()
                    val code = repository.getRegistrationCode(id)

                    call.respond(code)
                }.operation("getRegistrationCode", BadRequest, Unauthorized, Forbidden, NotFound)
                put {
                    val id = call.getId()
                    val code = call.receive<RegistrationCodeRequest>()
                    repository.updateRegistrationCode(id, code)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("updateRegistrationCode", BadRequest, Unauthorized, Forbidden, Conflict)
                delete {
                    val id = call.getId()
                    repository.deleteRegistrationCode(id)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("deleteRegistrationCode", BadRequest, Unauthorized, Forbidden, NotFound)
            }
        }
    }
}
