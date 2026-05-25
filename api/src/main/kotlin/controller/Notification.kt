package com.ctfp.controller

import com.ctfp.controller.helper.getId
import com.ctfp.dto.Notification
import com.ctfp.repository.INotificationRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.response.respond

fun Route.notification(repository: INotificationRepository) {
    route("/notification") {
        get {
            call.respond(repository.getAllNotifications())
        }
        get("/{id}") {
            val id = call.getId()
            val notification = repository.getNotification(id)

            call.respond(notification)
        }
        post {
            val notification = call.receive<Notification>()
            repository.createNotification(notification)

            call.respond(HttpStatusCode.Created)
        }
        put("/{id}") {
            val id = call.getId()
            val notification = call.receive<Notification>()
            repository.updateNotification(id, notification)

            call.respond(HttpStatusCode.NoContent)
        }
        delete("/{id}") {
            val id = call.getId()
            repository.deleteNotification(id)

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
