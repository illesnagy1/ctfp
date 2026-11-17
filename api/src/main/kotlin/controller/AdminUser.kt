package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.mergePatchOf
import com.ctfp.controller.helper.mergePatchesOf
import com.ctfp.controller.helper.operation
import com.ctfp.controller.helper.patched
import com.ctfp.controller.helper.receivePatched
import com.ctfp.controller.helper.validated
import com.ctfp.dto.AdminUserCreateRequest
import com.ctfp.dto.AdminUserUpdateRequest
import com.ctfp.dto.toUpdateRequest
import com.ctfp.plugins.adminOnly
import com.ctfp.service.ApiException
import com.ctfp.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.openapi.hide
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

/** Full user views and management, admins only. */
fun Route.adminUser(service: UserService) {
    adminOnly {
        route("/admin/users") {
            get {
                call.respond(service.getAllAdminUsers())
            }.operation("listAdminUsers", Unauthorized, Forbidden)
            post {
                service.createUser(call.receive<AdminUserCreateRequest>())

                call.respond(HttpStatusCode.Created)
            }.operation("createUser", BadRequest, Unauthorized, Forbidden, Conflict)
            put {
                service.updateUsers(call.receive<Map<Int, AdminUserUpdateRequest>>(), actorId = call.principal.id)

                call.respond(HttpStatusCode.NoContent)
            }.operation("updateUsers", BadRequest, Unauthorized, Forbidden, NotFound, Conflict)
            // Merge patches keyed by user id, e.g. {"3": {"isBanned": true}}; all or nothing, like the PUT.
            patch {
                val patches = call.receive<JsonObject>()
                val users =
                    patches.entries.associate { (key, patch) ->
                        val id = key.toIntOrNull() ?: throw ApiException(BadRequest, "Invalid user id $key")
                        id to patched(serializer<AdminUserUpdateRequest>(), service.getAdminUser(id).toUpdateRequest(), patch)
                    }
                service.updateUsers(validated(users), actorId = call.principal.id)

                call.respond(HttpStatusCode.NoContent)
            }.operation("patchUsers", BadRequest, Unauthorized, Forbidden, NotFound, Conflict).mergePatchesOf<AdminUserUpdateRequest>()
            delete {
            }.hide()

            route("/{id}") {
                get {
                    call.respond(service.getAdminUser(call.getId()))
                }.operation("getAdminUser", BadRequest, Unauthorized, Forbidden, NotFound)
                put {
                    service.updateUser(call.getId(), call.receive<AdminUserUpdateRequest>(), actorId = call.principal.id)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("updateUser", BadRequest, Unauthorized, Forbidden, NotFound, Conflict)
                // Only the fields sent change, e.g. {"isBanned": true}; null clears an optional one (JSON Merge Patch).
                patch {
                    val id = call.getId()
                    val user = call.receivePatched(service.getAdminUser(id).toUpdateRequest())
                    service.updateUser(id, user, actorId = call.principal.id)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("patchUser", BadRequest, Unauthorized, Forbidden, NotFound, Conflict)
                    .mergePatchOf<AdminUserUpdateRequest>()
                delete {
                    service.deleteUser(call.getId(), actorId = call.principal.id)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("deleteUser", BadRequest, Unauthorized, Forbidden, NotFound)
            }
        }
    }
}
