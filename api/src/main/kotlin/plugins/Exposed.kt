package com.ctfp.plugins

import com.ctfp.config.PostgresConfig
import com.ctfp.config.postgresConfig
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeTable
import com.ctfp.domain.model.FileTable
import com.ctfp.domain.model.RegistrationCodeTable
import com.ctfp.domain.model.UserTable
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

suspend fun Application.configureExposed() {
    connectToDatabase(environment.config.postgresConfig())

    createSchemas()
}

suspend fun createSchemas() = withTransaction {
    SchemaUtils.create(
        ChallengeTable,
        FileTable,
        RegistrationCodeTable,
        UserTable,
    )
}

fun Application.connectToDatabase(postgres: PostgresConfig): Database {
    if (postgres.embedded) {
        log.info("Using embedded H2 database for testing; set POSTGRES_EMBEDDED=false to use postgres")
        return Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            user = "root",
            password = ""
        )
    } else {
        log.info("Connecting to postgres database at ${postgres.url}")

        return Database.connect(
            url = postgres.url,
            user = postgres.user,
            password = postgres.password
        )
    }
}
