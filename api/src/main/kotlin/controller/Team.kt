package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.operation
import com.ctfp.dto.TeamRequest
import com.ctfp.plugins.adminOnly
import com.ctfp.service.TeamService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.hide
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.team(service: TeamService) {
    // Admins only for now: nothing here hides hidden or banned teams, and players can't manage their own yet.
    adminOnly {
        route("/teams") {
            get {
                call.respond(service.getAllTeams())
            }.operation("listTeams", Unauthorized, Forbidden)
            post {
                val team = call.receive<TeamRequest>()
                service.createTeam(team)

                call.respond(HttpStatusCode.Created)
            }.operation("createTeam", BadRequest, Unauthorized, Forbidden, Conflict)
            route("/{id}") {
                get {
                    val id = call.getId()
                    val team = service.getTeam(id)

                    call.respond(team)
                }.operation("getTeam", BadRequest, Unauthorized, Forbidden, NotFound)
                put {
                    val id = call.getId()
                    val team = call.receive<TeamRequest>()
                    service.updateTeam(id, team)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("updateTeam", BadRequest, Unauthorized, Forbidden, Conflict)
                delete {
                    val id = call.getId()
                    service.deleteTeam(id)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("deleteTeam", BadRequest, Unauthorized, Forbidden, NotFound)
            }
        }
    }
}
