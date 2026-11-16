package com.ctfp.plugins

import com.ctfp.config.PostgresConfig
import com.ctfp.config.postgresConfig
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeTable
import com.ctfp.domain.model.FileTable
import com.ctfp.domain.model.FlagTable
import com.ctfp.domain.model.PasswordResetTokenTable
import com.ctfp.domain.model.RegistrationCodeTable
import com.ctfp.domain.model.SubmissionTable
import com.ctfp.domain.model.TeamTable
import com.ctfp.domain.model.UserTable
import io.ktor.server.application.Application
import io.ktor.server.application.log
import org.jetbrains.exposed.v1.core.vendors.H2Dialect
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

suspend fun Application.configureExposed() {
    connectToDatabase(environment.config.postgresConfig())

    createSchemas()
}

suspend fun createSchemas() =
    withTransaction {
        SchemaUtils.create(
            ChallengeTable,
            FileTable,
            FlagTable,
            PasswordResetTokenTable,
            TeamTable,
            SubmissionTable,
            RegistrationCodeTable,
            UserTable,
        )
    }

private const val OTEL_DRIVER = "io.opentelemetry.instrumentation.jdbc.OpenTelemetryDriver"

/** Routes JDBC through the OpenTelemetry driver (set up in configureMonitoring) so SQL statements become spans. */
private fun traced(url: String): String {
    // Exposed picks driver and dialect by URL prefix, so it has to learn the jdbc:otel: ones.
    Database.registerJdbcDriver("jdbc:otel:h2", OTEL_DRIVER, H2Dialect.dialectName)
    Database.registerJdbcDriver("jdbc:otel:postgresql", OTEL_DRIVER, PostgreSQLDialect.dialectName)
    return url.replaceFirst("jdbc:", "jdbc:otel:")
}

fun Application.connectToDatabase(postgres: PostgresConfig): Database {
    if (postgres.embedded) {
        log.info("Using embedded H2 database for testing; set POSTGRES_EMBEDDED=false to use postgres")
        return Database.connect(
            url = traced("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
            user = "root",
            password = "",
        )
    } else {
        log.info("Connecting to postgres database at ${postgres.url}")

        return Database.connect(
            url = traced(postgres.url),
            user = postgres.user,
            password = postgres.password,
        )
    }
}
