package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.controller.helper.operation
import com.ctfp.dto.NotificationRequest
import com.ctfp.plugins.adminOnly
import com.ctfp.service.NotificationService
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
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

fun Route.notification(service: NotificationService) {
    // Admins only for now: nothing here holds back notifications scheduled for later (pushAt).
    adminOnly {
        route("/notifications") {
            get {
                call.respond(service.getAllNotifications())
            }.operation("listNotifications", Unauthorized, Forbidden)
            post {
                val notification = call.receive<NotificationRequest>()
                service.createNotification(notification)

                call.respond(HttpStatusCode.Created)
            }.operation("createNotification", BadRequest, Unauthorized, Forbidden)
            route("/{id}") {
                get {
                    val id = call.getId()
                    val notification = service.getNotification(id)

                    call.respond(notification)
                }.operation("getNotification", BadRequest, Unauthorized, Forbidden, NotFound)
                put {
                    val id = call.getId()
                    val notification = call.receive<NotificationRequest>()
                    service.updateNotification(id, notification)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("updateNotification", BadRequest, Unauthorized, Forbidden)
                delete {
                    val id = call.getId()
                    service.deleteNotification(id)

                    call.respond(HttpStatusCode.NoContent)
                }.operation("deleteNotification", BadRequest, Unauthorized, Forbidden, NotFound)
            }
        }
    }
}
