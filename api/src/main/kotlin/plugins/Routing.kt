package com.ctfp.plugins

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
import com.ctfp.service.AuthService
import com.ctfp.service.ChallengeService
import com.ctfp.service.EmailTemplateService
import com.ctfp.service.FileService
import com.ctfp.service.FlagService
import com.ctfp.service.HintService
import com.ctfp.service.NotificationService
import com.ctfp.service.PageService
import com.ctfp.service.RegistrationCodeService
import com.ctfp.service.SubmissionService
import com.ctfp.service.TeamService
import com.ctfp.service.UserService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }

        challenge(ChallengeService())
        auth(AuthService())
        emailTemplate(EmailTemplateService())
        file(FileService())
        flag(FlagService())
        hint(HintService())
        notification(NotificationService())
        page(PageService())
        registrationCode(RegistrationCodeService())
        submission(SubmissionService())
        team(TeamService())
        user(UserService())

        staticResources("/static", "static")
    }
}
