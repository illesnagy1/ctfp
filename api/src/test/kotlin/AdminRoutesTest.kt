package com.ctfp

import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class AdminRoutesTest {
    /** Routes that are admins only, reading included, until they filter what players may see. */
    private val routes =
        listOf("email-templates", "hints?challengeId=1", "notifications", "pages", "registration-codes", "teams")
            .flatMap { listOf(HttpMethod.Get to "/v1/$it", HttpMethod.Post to "/v1/${it.substringBefore('?')}") } +
            listOf("email-templates", "hints", "notifications", "pages", "registration-codes", "teams").flatMap {
                listOf(HttpMethod.Get, HttpMethod.Put, HttpMethod.Delete).map { method -> method to "/v1/$it/1" }
            }

    @Test
    fun `guests and players are refused, admins let through`() =
        testApplication {
            configure()
            startApplication()
            val admin = registerAdmin("admin-routes-admin")
            val player = register("admin-routes-player")

            for ((method, path) in routes) {
                assertEquals(HttpStatusCode.Unauthorized, call(method, path).status, "$method $path as a guest")
                assertEquals(HttpStatusCode.Forbidden, call(method, path, player).status, "$method $path as a player")
            }
            assertEquals(HttpStatusCode.OK, client.get("/v1/registration-codes") { bearerAuth(admin) }.status)
        }
}
