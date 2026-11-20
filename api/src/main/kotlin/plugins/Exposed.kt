package com.ctfp.plugins

import com.ctfp.config.PostgresConfig
import com.ctfp.config.postgresConfig
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeStartTable
import com.ctfp.domain.model.ChallengeTable
import com.ctfp.domain.model.EmailTemplateTable
import com.ctfp.domain.model.FileTable
import com.ctfp.domain.model.FlagTable
import com.ctfp.domain.model.HintTable
import com.ctfp.domain.model.HintUnlockTable
import com.ctfp.domain.model.MagicLinkTable
import com.ctfp.domain.model.NotificationTable
import com.ctfp.domain.model.PageTable
import com.ctfp.domain.model.PasswordResetTokenTable
import com.ctfp.domain.model.RegistrationCodeTable
import com.ctfp.domain.model.SubmissionTable
import com.ctfp.domain.model.TeamMemberTable
import com.ctfp.domain.model.TeamTable
import com.ctfp.domain.model.UserTable
import io.ktor.server.application.Application
import io.ktor.server.application.log
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.core.vendors.H2Dialect
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

/** Every table the app uses; `./gradlew generateMigration` diffs these against the database. */
val allTables =
    arrayOf(
        ChallengeTable,
        ChallengeStartTable,
        EmailTemplateTable,
        FileTable,
        FlagTable,
        HintTable,
        HintUnlockTable,
        MagicLinkTable,
        NotificationTable,
        PageTable,
        PasswordResetTokenTable,
        RegistrationCodeTable,
        SubmissionTable,
        TeamMemberTable,
        TeamTable,
        UserTable,
    )

suspend fun Application.configureExposed() {
    val postgres = environment.config.postgresConfig()
    connectToDatabase(postgres)

    if (postgres.embedded) {
        // The migrations are PostgreSQL SQL, so the throwaway H2 database gets its schema straight from the tables.
        withTransaction { SchemaUtils.create(*allTables) }
    } else {
        migrate(postgres)
    }
}

/** Applies the pending db/migration scripts, so the schema always matches the tables of this build. */
fun migrate(postgres: PostgresConfig) {
    Flyway
        .configure()
        .dataSource(postgres.url, postgres.user, postgres.password)
        .load()
        .migrate()
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
