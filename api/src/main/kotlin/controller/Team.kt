package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.Team
import com.ctfp.service.TeamService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.team(service: TeamService) {
    route("/team") {
        get {
            call.respond(service.getAllTeams())
        }
        get("/{id}") {
            val id = call.getId()
            val team = service.getTeam(id)

            call.respond(team)
        }
        post {
            val team = call.receive<Team>()
            service.createTeam(team)

            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.getId()
            val team = call.receive<Team>()
            service.updateTeam(id, team)

            call.respond(HttpStatusCode.NoContent)
        }
        delete("/{id}") {
            val id = call.getId()
            service.deleteTeam(id)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
