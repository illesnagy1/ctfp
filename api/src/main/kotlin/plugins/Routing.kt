package com.ctfp.plugins

import com.ctfp.config.jwtConfig
import com.ctfp.config.settingsFileConfig
import com.ctfp.config.uploadsConfig
import com.ctfp.storage.LocalFileStorage
import com.ctfp.controller.adminUser
import com.ctfp.controller.auth
import com.ctfp.controller.challenge
import com.ctfp.controller.scoreboard
import com.ctfp.controller.emailTemplate
import com.ctfp.controller.file
import com.ctfp.controller.flag
import com.ctfp.controller.hint
import com.ctfp.controller.notification
import com.ctfp.controller.page
import com.ctfp.controller.registrationCode
import com.ctfp.controller.settings
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
import com.ctfp.service.helper.PasswordHasher
import com.ctfp.service.RegistrationCodeService
import com.ctfp.service.SettingsService
import com.ctfp.service.ScoreboardService
import com.ctfp.service.SubmissionService
import com.ctfp.service.TeamService
import com.ctfp.service.UserService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import kotlin.io.path.Path

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello, World!")
        }

        val hasher = PasswordHasher()
        val userService = UserService(hasher)
        val uploads = environment.config.uploadsConfig()
        val storage = LocalFileStorage(Path(uploads.dir))
        val challengeService = ChallengeService(storage)

        route("v1") {
            challenge(challengeService)
            auth(AuthService(environment.config.jwtConfig(), hasher))
            emailTemplate(EmailTemplateService())
            file(FileService(storage, challengeService, uploads.maxBytes))
            flag(FlagService())
            hint(HintService())
            notification(NotificationService())
            page(PageService())
            registrationCode(RegistrationCodeService())
            settings(SettingsService(Path(environment.config.settingsFileConfig().file)))
            submission(SubmissionService(challengeService))
            scoreboard(ScoreboardService())
            team(TeamService())
            user(userService)
            adminUser(userService)
        }

        staticResources("/static", "static")
    }
}
