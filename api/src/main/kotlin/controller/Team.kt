package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.TeamRequest
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
    route("/teams") {
        get {
            call.respond(service.getAllTeams())
        }
        post {
            val team = call.receive<TeamRequest>()
            service.createTeam(team)

            call.respond(HttpStatusCode.Created)
        }
        put {

        }
        delete {

        }
        route("/{id}") {
            get {
                val id = call.getId()
                val team = service.getTeam(id)

                call.respond(team)
            }
            put {
                val id = call.getId()
                val team = call.receive<TeamRequest>()
                service.updateTeam(id, team)

                call.respond(HttpStatusCode.NoContent)
            }
            delete {
                val id = call.getId()
                service.deleteTeam(id)

                call.respond(HttpStatusCode.NoContent)
            }

            route("/submissions") {
                get {

                }
                post {

                }
                put {

                }
                delete {

                }
            }

            route("users") {
                get {

                }
                post {

                }
                put {

                }
                delete {

                }
            }
        }
    }
}
