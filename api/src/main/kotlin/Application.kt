package com.ctfp

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureSockets()
    configureDatabases()
    configureHTTP()
    configureRouting()
}
