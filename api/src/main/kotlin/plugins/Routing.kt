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
import com.ctfp.repository.postgres.PostgresChallengeRepository
import com.ctfp.repository.postgres.PostgresEmailTemplateRepository
import com.ctfp.repository.postgres.PostgresFileRepository
import com.ctfp.repository.postgres.PostgresFlagRepository
import com.ctfp.repository.postgres.PostgresHintRepository
import com.ctfp.repository.postgres.PostgresNotificationRepository
import com.ctfp.repository.postgres.PostgresPageRepository
import com.ctfp.repository.postgres.PostgresRegistrationCodeRepository
import com.ctfp.repository.postgres.PostgresSubmissionRepository
import com.ctfp.repository.postgres.PostgresTeamRepository
import com.ctfp.repository.postgres.PostgresUserRepository
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        challenge(
            PostgresChallengeRepository(),
            PostgresFlagRepository(),
            PostgresHintRepository()
        )
        auth()
        emailTemplate(PostgresEmailTemplateRepository())
        file(PostgresFileRepository())
        flag(PostgresFlagRepository())
        hint(PostgresHintRepository())
        notification(PostgresNotificationRepository())
        page(PostgresPageRepository())
        registrationCode(PostgresRegistrationCodeRepository())
        submission(PostgresSubmissionRepository())
        team(PostgresTeamRepository())
        user(PostgresUserRepository())
    }
}
