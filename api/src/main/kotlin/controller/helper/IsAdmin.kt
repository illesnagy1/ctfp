package com.ctfp.controller.helper

import com.ctfp.plugins.userId
import com.ctfp.service.helper.isAdmin
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

/** Whether the caller is a logged-in admin; for routes inside `authenticate(optional = true) { }`. */
suspend fun ApplicationCall.isAdmin() = principal<JWTPrincipal>() != null && isAdmin(userId())
