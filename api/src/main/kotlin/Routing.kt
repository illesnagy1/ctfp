package com.ctfp

import com.ctfp.controller.auth
import com.ctfp.controller.challenge
import com.ctfp.controller.emailTemplate
import com.ctfp.controller.file
import com.ctfp.controller.flag
import com.ctfp.controller.hint
import com.ctfp.controller.notification
import com.ctfp.controller.page
import com.ctfp.controller.registrationCode
import com.ctfp.controller.submission
import com.ctfp.controller.team
import com.ctfp.controller.user
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        challenge()
        auth()
        emailTemplate()
        file()
        flag()
        hint()
        notification()
        page()
        registrationCode()
        submission()
        team()
        user()
    }
}
