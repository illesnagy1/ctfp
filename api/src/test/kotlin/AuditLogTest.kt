package com.ctfp

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.ktor.http.HttpMethod
import io.ktor.server.testing.testApplication
import org.slf4j.LoggerFactory
import kotlin.test.Test
import kotlin.test.assertTrue

class AuditLogTest {
    @Test
    fun `security events reach the audit log`() =
        testApplication {
            val logger = LoggerFactory.getLogger("audit") as Logger
            val events = ListAppender<ILoggingEvent>().apply { start() }
            logger.addAppender(events)
            try {
                configure()
                startApplication()
                val admin = registerAdmin("audit-admin")
                val player = register("audit-player")
                call(HttpMethod.Post, "/v1/auth/login", body = """{"login":"audit-player","password":"wrong-password"}""")
                call(HttpMethod.Get, "/v1/admin/users", player)
                call(HttpMethod.Put, "/v1/admin/settings", admin, """{"ctfName":"Audited"}""")
                call(HttpMethod.Put, "/v1/admin/settings", admin, """{"ctfName":"CTFP"}""")

                val messages = events.list.map { it.formattedMessage }
                for (event in listOf("user_created:", "authn_login_fail:", "authz_fail:", "authz_admin:")) {
                    assertTrue(messages.any { it.startsWith(event) }, "$event in $messages")
                }
                assertTrue(messages.any { it.startsWith("authz_admin:") && "PUT /v1/admin/settings" in it && "status=200" in it })
            } finally {
                logger.detachAppender(events)
            }
        }
}
