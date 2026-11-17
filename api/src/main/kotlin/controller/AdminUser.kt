package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.ifMatch
import com.ctfp.controller.helper.mergePatchOf
import com.ctfp.controller.helper.mergePatchesOf
import com.ctfp.controller.helper.operation
import com.ctfp.dto.AdminUserCreateRequest
import com.ctfp.dto.AdminUserUpdateRequest
import com.ctfp.dto.etag
import com.ctfp.plugins.adminOnly
import com.ctfp.service.ApiException
import com.ctfp.service.UserService
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.PreconditionFailed
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.header
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
                val patches =
                    call.receive<JsonObject>().entries.associate { (key, patch) ->
                        val id = key.toIntOrNull() ?: throw ApiException(BadRequest, "Invalid user id $key")
                        id to (patch as? JsonObject ?: throw ApiException(BadRequest, "The patch for user $id must be an object"))
                    }
                service.patchUsers(patches, actorId = call.principal.id)

                call.respond(HttpStatusCode.NoContent)
            }.operation("patchUsers", BadRequest, Unauthorized, Forbidden, NotFound, Conflict).mergePatchesOf<AdminUserUpdateRequest>()
            delete {
            }.hide()

            route("/{id}") {
                get {
                    val user = service.getAdminUser(call.getId())
                    call.response.header(HttpHeaders.ETag, user.etag)
                    call.respond(user)
                }.operation("getAdminUser", BadRequest, Unauthorized, Forbidden, NotFound)
                // With If-Match (the ETag read), 412 if the account changed since.
                put {
                    val user = call.receive<AdminUserUpdateRequest>()
                    service.updateUser(call.getId(), user, actorId = call.principal.id, ifMatch = call.ifMatch)
                    call.respond(HttpStatusCode.NoContent)
                }.operation("updateUser", BadRequest, Unauthorized, Forbidden, NotFound, Conflict, PreconditionFailed)
                // Only the fields sent change, e.g. {"isBanned": true}; null clears an optional one (JSON Merge Patch).
                patch {
                    val patch = call.receive<JsonObject>()
                    service.patchUser(call.getId(), patch, actorId = call.principal.id, ifMatch = call.ifMatch)
                    call.respond(HttpStatusCode.NoContent)
                }.operation("patchUser", BadRequest, Unauthorized, Forbidden, NotFound, Conflict, PreconditionFailed)
                    .mergePatchOf<AdminUserUpdateRequest>()
                delete {
                    service.deleteUser(call.getId(), actorId = call.principal.id)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("deleteUser", BadRequest, Unauthorized, Forbidden, NotFound)
            }
        }
    }
}
