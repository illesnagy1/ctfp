package com.ctfp.plugins

import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeTable
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

suspend fun Application.configureExposed() {
    connectToDatabase(true)

    createSchemas()
}

suspend fun createSchemas() = withTransaction {
    SchemaUtils.create(
        ChallengeTable,
    )
}

fun Application.connectToDatabase(embedded: Boolean): Database {
    if (embedded) {
        log.info("Using embedded H2 database for testing; replace this flag to use postgres")
        return Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            user = "root",
            password = ""
        )
    } else {
        val url = environment.config.property("postgres.url").getString()
        log.info("Connecting to postgres database at $url")
        val user = environment.config.property("postgres.user").getString()
        val password = environment.config.property("postgres.password").getString()

        return Database.connect(
            url = url,
            user = user,
            password = password
        )
    }
}
