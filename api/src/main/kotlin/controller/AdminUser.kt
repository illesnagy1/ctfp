package com.ctfp.controller

import com.ctfp.controller.helper.RequireAdmin
import com.ctfp.controller.helper.errors
import com.ctfp.controller.helper.getId
import com.ctfp.dto.AdminUserCreateRequest
import com.ctfp.dto.AdminUserUpdateRequest
import com.ctfp.plugins.userId
import com.ctfp.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

/** Full user views and management, admins only. */
fun Route.adminUser(service: UserService) {
    authenticate {
        route("/admin/users") {
            install(RequireAdmin)

            get {
                call.respond(service.getAllAdminUsers())
            }.errors(Unauthorized, Forbidden)
            post {
                service.createUser(call.receive<AdminUserCreateRequest>())

                call.respond(HttpStatusCode.Created)
            }.errors(BadRequest, Unauthorized, Forbidden, Conflict)
            put {
                service.updateUsers(call.receive<Map<Int, AdminUserUpdateRequest>>(), actorId = call.userId())

                call.respond(HttpStatusCode.NoContent)
            }.errors(BadRequest, Unauthorized, Forbidden, NotFound, Conflict)
            delete {

            }

            route("/{id}") {
                get {
                    call.respond(service.getAdminUser(call.getId()))
                }.errors(BadRequest, Unauthorized, Forbidden, NotFound)
                put {
                    service.updateUser(call.getId(), call.receive<AdminUserUpdateRequest>(), actorId = call.userId())

                    call.respond(HttpStatusCode.NoContent)
                }.errors(BadRequest, Unauthorized, Forbidden, NotFound, Conflict)
                delete {
                    service.deleteUser(call.getId())

                    call.respond(HttpStatusCode.NoContent)
                }.errors(BadRequest, Unauthorized, Forbidden, NotFound)
            }
        }
    }
}
