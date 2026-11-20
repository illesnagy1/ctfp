package com.ctfp

import com.ctfp.config.PostgresConfig
import com.ctfp.plugins.allTables
import com.ctfp.plugins.migrate
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import java.io.File

private val VERSIONED = Regex("""V(\d+)__.+\.sql""")

/** Relative to the api project, Gradle's working directory for JavaExec; Flyway reads it as classpath:db/migration. */
private const val MIGRATIONS_DIRECTORY = "src/main/resources/db/migration"

/**
 * Writes `V<next>__<name>.sql` with the SQL that brings the database up to the Exposed tables. Run with
 * `./gradlew generateMigration --args="<name>"` against a scratch Postgres (POSTGRES_URL, POSTGRES_USER,
 * POSTGRES_PASSWORD): the existing migrations are applied first, so the diff holds only the new changes. Review the
 * file before committing it: Exposed may emit DROP statements, and a renamed column shows up as drop + add.
 */
@OptIn(ExperimentalDatabaseMigrationApi::class)
fun main(args: Array<String>) {
    val directory = File(MIGRATIONS_DIRECTORY)
    val name = args.firstOrNull() ?: error("Usage: ./gradlew generateMigration --args=\"<name>\", e.g. add_user_avatar")
    val postgres =
        PostgresConfig(
            embedded = false,
            url = System.getenv("POSTGRES_URL") ?: "jdbc:postgresql://localhost:5432/ctfp",
            user = System.getenv("POSTGRES_USER") ?: "ctfp",
            password = System.getenv("POSTGRES_PASSWORD") ?: "ctfp",
        )

    migrate(postgres)
    val database = Database.connect(postgres.url, user = postgres.user, password = postgres.password)
    // shortcut: drops every CREATE SEQUENCE, since SERIAL ids make their own and Exposed misreports them (e.g. the quoted
    // "User_id_seq") as missing; stop filtering once a table uses an explicit Sequence.
    val statements =
        transaction(database) { MigrationUtils.statementsRequiredForDatabaseMigration(*allTables) }
            .filterNot { it.startsWith("CREATE SEQUENCE") }
    if (statements.isEmpty()) {
        println("Database already matches the tables; no migration written")
        return
    }

    val versions = directory.listFiles().orEmpty().mapNotNull { VERSIONED.matchEntire(it.name)?.groupValues?.get(1)?.toInt() }
    val next = (versions.maxOrNull() ?: 0) + 1
    val file = directory.resolve("V${next}__$name.sql")
    file.writeText(statements.joinToString(";\n", postfix = ";\n"))
    println("Wrote ${file.path}")
}
