package com.ctfp.controller

import com.ctfp.controller.helper.operation
import com.ctfp.dto.CtftimeFeed
import com.ctfp.dto.toCtftimeFeed
import com.ctfp.plugins.isAdmin
import com.ctfp.plugins.optionallyAuthenticated
import com.ctfp.service.ScoreboardService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.openapi.jsonSchema
import io.ktor.server.auth.principalOrNull
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import kotlinx.serialization.json.Json

private val feedJson = Json { explicitNulls = false }

@OptIn(ExperimentalKtorApi::class)
fun Route.scoreboard(service: ScoreboardService) {
    // Public, like the challenge list; frozen at the settings' freezeAt, except for admins.
    optionallyAuthenticated {
        get("/scoreboard") {
            call.respond(service.get(live = call.principalOrNull.isAdmin))
        }.operation("getScoreboard")
    }

    // The same ranking for CTFtime to import (it polls every minute); public too.
    get("/ctftime/scoreboard") {
        val feed = feedJson.encodeToString(service.get().toCtftimeFeed())
        call.respondText(escapeNonAscii(feed), ContentType.Application.Json)
    }.operation("getCtftimeScoreboard")
        .describe { responses { HttpStatusCode.OK { schema = jsonSchema<CtftimeFeed>() } } }
}

private const val FIRST_NON_ASCII = 0x80

/** CTFtime wants non-ASCII characters (e.g. in team names) as `\uXXXX`; in JSON text they can only be in strings. */
internal fun escapeNonAscii(json: String) =
    buildString(json.length) {
        for (char in json)
            if (char.code < FIRST_NON_ASCII) append(char)
            else append("\\u%04x".format(char.code))
    }
