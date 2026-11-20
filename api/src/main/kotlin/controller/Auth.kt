package com.ctfp.controller

import com.ctfp.controller.helper.operation
import com.ctfp.dto.ForgotPasswordRequest
import com.ctfp.dto.LoginRequest
import com.ctfp.dto.RegisterRequest
import com.ctfp.dto.ResetPasswordRequest
import com.ctfp.plugins.AuthRateLimit
import com.ctfp.plugins.authenticated
import com.ctfp.service.AuthService
import com.ctfp.service.PasswordResetService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.header
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
        // Guessing passwords and flooding inboxes; see RateLimiting.kt.
        rateLimit(AuthRateLimit) {
            post("login") {
                val request = call.receive<LoginRequest>()
                // The body carries a bearer token; nothing on the way may keep a copy.
                call.response.header(HttpHeaders.CacheControl, "no-store")
                call.respond(service.login(request))
            }.operation("login", BadRequest, Unauthorized, Forbidden)
            post("register") {
                val request = call.receive<RegisterRequest>()
                call.response.header(HttpHeaders.CacheControl, "no-store")
                call.respond(HttpStatusCode.Created, service.register(request))
            }.operation("register", BadRequest, Forbidden, Conflict)
            // 202 whether or not the address has an account, so it can't be used to find out.
            post("forgot-password") {
                resets.request(call.receive<ForgotPasswordRequest>().email)
                call.respond(HttpStatusCode.Accepted)
            }.operation("forgotPassword", BadRequest)
            post("reset-password") {
                val request = call.receive<ResetPasswordRequest>()
                resets.reset(request.token, request.password)
                call.respond(HttpStatusCode.NoContent)
            }.operation("resetPassword", BadRequest)
        }
        authenticated {
            get("me") {
                call.respond(service.me(call.principal.id))
            }.operation("getAuthUser", Unauthorized)
        }
    }
}
