package com.ctfp.config.enum

import io.ktor.server.auth.AuthenticationRole

enum class UserRole : AuthenticationRole {
    ADMIN,
    USER,
}
