package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.AdminUserCreateRequest
import com.ctfp.dto.AdminUserUpdateRequest
import com.ctfp.dto.UserUpdateRequest
import com.ctfp.plugins.userId
import com.ctfp.service.ApiException
import com.ctfp.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.user(service: UserService) {
    authenticate {
        route("/users") {
            get {
                if (service.isAdmin(call.userId())) {
                    call.respond(service.getAllAdminUsers())
                } else {
                    call.respond(service.getAllUsers())
                }
            }
            post {
                service.requireAdmin(call.userId())
                val user = call.receive<AdminUserCreateRequest>()
                service.createUser(user)

                call.respond(HttpStatusCode.Created)
            }
            put {
                service.requireAdmin(call.userId())
                val users = call.receive<Map<Int, AdminUserUpdateRequest>>()
                service.updateUsers(users)
                call.respond(HttpStatusCode.NoContent)
            }
            delete {

            }
            route("/{id}") {
                get {
                    val id = call.getId()
                    val callerId = call.userId()

                    if (callerId == id || service.isAdmin(callerId)) {
                        call.respond(service.getAdminUser(id))
                    } else {
                        call.respond(service.getUser(id))
                    }
                }
                put {
                    val id = call.getId()
                    val callerId = call.userId()
                    when {
                        service.isAdmin(callerId) -> service.updateUser(id, call.receive<AdminUserUpdateRequest>())
                        callerId == id -> service.updateUser(id, call.receive<UserUpdateRequest>())
                        else -> throw ApiException(HttpStatusCode.Forbidden, "You can only update your own account")
                    }

                    call.respond(HttpStatusCode.NoContent)
                }
                delete {
                    service.requireAdmin(call.userId())
                    val id = call.getId()
                    service.deleteUser(id)

                    call.respond(HttpStatusCode.NoContent)
                }

                route("submissions") {
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
}
