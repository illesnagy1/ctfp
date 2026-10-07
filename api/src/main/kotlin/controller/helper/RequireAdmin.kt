package com.ctfp.controller.helper

import com.ctfp.plugins.userId
import com.ctfp.service.helper.requireAdmin
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

/**
 * Rejects non-admins with 403 on every route below the one it's installed on. Install it inside `authenticate { }`;
 * unauthenticated calls are already answered with 401 by then.
 */
val RequireAdmin = createRouteScopedPlugin("RequireAdmin") {
    on(AuthenticationChecked) { call ->
        if (call.principal<JWTPrincipal>() != null) requireAdmin(call.userId())
    }
}
