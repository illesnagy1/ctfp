package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.NotificationRequest
import com.ctfp.service.NotificationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.notification(service: NotificationService) {
    route("/notifications") {
        get {
            call.respond(service.getAllNotifications())
        }
        post {
            val notification = call.receive<NotificationRequest>()
            service.createNotification(notification)

            call.respond(HttpStatusCode.Created)
        }
        put {

        }
        delete {

        }
        route("/{id}") {
            get {
                val id = call.getId()
                val notification = service.getNotification(id)

                call.respond(notification)
            }
            put {
                val id = call.getId()
                val notification = call.receive<NotificationRequest>()
                service.updateNotification(id, notification)

                call.respond(HttpStatusCode.NoContent)
            }
            delete {
                val id = call.getId()
                service.deleteNotification(id)

                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
