package com.ctfp.controller

import com.ctfp.controller.helper.errors
import com.ctfp.dto.ForgotPasswordRequest
import com.ctfp.dto.LoginRequest
import com.ctfp.dto.RegisterRequest
import com.ctfp.dto.ResetPasswordRequest
import com.ctfp.plugins.authenticated
import com.ctfp.service.AuthService
import com.ctfp.service.PasswordResetService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.auth(
    service: AuthService,
    resets: PasswordResetService,
) {
    route("/auth") {
        post("login") {
            val request = call.receive<LoginRequest>()
            call.respond(service.login(request))
        }.errors(BadRequest, Unauthorized, Forbidden)
        post("register") {
            val request = call.receive<RegisterRequest>()
            call.respond(HttpStatusCode.Created, service.register(request))
        }.errors(BadRequest, Conflict)
        // 202 whether or not the address has an account, so it can't be used to find out.
        post("forgot-password") {
            resets.request(call.receive<ForgotPasswordRequest>().email)
            call.respond(HttpStatusCode.Accepted)
        }
        post("reset-password") {
            val request = call.receive<ResetPasswordRequest>()
            resets.reset(request.token, request.password)
            call.respond(HttpStatusCode.NoContent)
        }.errors(BadRequest)
        authenticated {
            get("me") {
                call.respond(service.me(call.principal.id))
            }.errors(Unauthorized)
        }
    }
}
