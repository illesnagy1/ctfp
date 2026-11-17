package com.ctfp

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ServerTest {
    @Test
    fun `metrics endpoint exposes http and jvm metrics`() =
        testApplication {
            configure()
            assertNotNull(client.get("/livez").headers[HttpHeaders.XRequestId])
            val body = client.get("/metrics").bodyAsText()
            assertTrue("ktor_http_server_requests_seconds_count" in body, body)
            assertTrue("jvm_memory_used_bytes" in body, body)
        }

    @Test
    fun `health probes report ok`() =
        testApplication {
            configure()
            assertEquals(HttpStatusCode.OK, client.get("/livez").status)
            assertEquals(HttpStatusCode.OK, client.get("/readyz").status)
        }
}
