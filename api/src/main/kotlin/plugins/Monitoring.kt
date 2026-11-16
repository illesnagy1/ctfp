package com.ctfp.plugins

import com.ctfp.domain.db.withTransaction
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.path
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.composite.CompositeMeterRegistry
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.micrometer.registry.otlp.OtlpMeterRegistry
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.jdbc.OpenTelemetryDriver
import io.opentelemetry.instrumentation.ktor.v3_0.KtorServerTelemetry
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import java.sql.SQLException
import java.util.UUID


fun Application.configureMonitoring() {
    val otlpEnv = listOf("OTEL_EXPORTER_OTLP_ENDPOINT", "OTEL_EXPORTER_OTLP_METRICS_ENDPOINT")
    val otlpEnabled = otlpEnv.any { !System.getenv(it).isNullOrBlank() }

    // Reads the standard OTEL_* env vars; registers a JVM shutdown hook that flushes pending spans.
    val openTelemetry =
        if (otlpEnabled) {
            AutoConfiguredOpenTelemetrySdk
                .builder()
                // Defaults only, env vars win. Micrometer exports the metrics, logs go to Loki via stdout.
                // http/protobuf (not the SDK's grpc default) so traces share Micrometer's OTLP/HTTP endpoint.
                .addPropertiesSupplier {
                    mapOf(
                        "otel.metrics.exporter" to "none",
                        "otel.logs.exporter" to "none",
                        "otel.exporter.otlp.protocol" to "http/protobuf",
                    )
                }
                .build()
                .openTelemetrySdk
        } else {
            OpenTelemetry.noop()
        }
    // Must run before configureExposed opens connections: SQL statements become spans.
    OpenTelemetryDriver.install(openTelemetry)
    install(KtorServerTelemetry) {
        setOpenTelemetry(openTelemetry)
    }

    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val registry = CompositeMeterRegistry().add(prometheus)
    // OtlpMeterRegistry reads the standard OTEL_* env vars (endpoint, headers, OTEL_SERVICE_NAME, interval).
    if (otlpEnabled) registry.add(OtlpMeterRegistry())
    // Closing publishes the last OTLP batch.
    monitor.subscribe(ApplicationStopped) { registry.close() }

    install(MicrometerMetrics) {
        this.registry = registry
    }
    install(CallId) {
        generate { UUID.randomUUID().toString() }
        replyToHeader(HttpHeaders.XRequestId)
    }
    install(CallLogging) {
        callIdMdc("call_id")
        disableDefaultColors() // ANSI escapes would end up inside JSON messages
        filter { it.request.path() !in setOf("/metrics", "/livez", "/readyz") }
    }

    routing {
        get("/metrics") {
            call.respondText(prometheus.scrape())
        }
        get("/livez") {
            call.respondText("ok")
        }
        get("/readyz") {
            try {
                withTransaction { exec("SELECT 1") }
                call.respondText("ok")
            } catch (e: SQLException) {
                log.warn("Readiness check failed: database unavailable", e)
                call.respondText("database unavailable", status = HttpStatusCode.ServiceUnavailable)
            }
        }
    }
}
