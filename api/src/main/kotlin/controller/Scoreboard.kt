package com.ctfp.controller

import com.ctfp.service.ScoreboardService
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.scoreboard(service: ScoreboardService) {
    // Public, like the challenge list.
    get("/scoreboard") {
        call.respond(service.get())
    }
}
