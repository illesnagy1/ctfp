package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.RegistrationCode
import com.ctfp.repository.postgres.PostgresRegistrationCodeRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.registrationCode(repository: PostgresRegistrationCodeRepository) {
    route("/registration-code") {
        get {
            call.respond(repository.getAllRegistrationCodes())
        }
        get("/{id}") {
            val id = call.getId()
            val code = repository.getRegistrationCode(id)

            call.respond(code)
        }
        post {
            val code = call.receive<RegistrationCode>()
            repository.createRegistrationCode(code)

            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.getId()
            val code = call.receive<RegistrationCode>()
            repository.updateRegistrationCode(id, code)

            call.respond(HttpStatusCode.NoContent)
        }
        delete("/{id}") {
            val id = call.getId()
            repository.deleteRegistrationCode(id)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
