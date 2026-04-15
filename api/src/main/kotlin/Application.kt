package com.ctfp

import com.ctfp.plugins.configureDatabases
import com.ctfp.plugins.configureHTTP
import com.ctfp.plugins.configureRouting
import com.ctfp.plugins.configureSecurity
import com.ctfp.plugins.configureSockets
import io.ktor.server.application.*
import org.flywaydb.core.Flyway

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val url = environment.config.property("postgres.url").getString()
    log.info("Connecting to postgres database at $url")
    val user = environment.config.property("postgres.user").getString()
    val password = environment.config.property("postgres.password").getString()
    Flyway.configure().dataSource(url, user, password).load().migrate()
    configureSecurity()
    configureSockets()
    configureDatabases()
    configureHTTP()
    configureRouting()
}
