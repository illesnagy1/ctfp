package com.ctfp.plugins

import com.codahale.metrics.Slf4jReporter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.dropwizard.DropwizardMetrics
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private const val METRICS_REPORT_INTERVAL_SECONDS = 10L

fun Application.configureMonitoring() {
    install(DropwizardMetrics) {
        Slf4jReporter
            .forRegistry(registry)
            .outputTo(LoggerFactory.getLogger("metrics"))
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
            .start(METRICS_REPORT_INTERVAL_SECONDS, TimeUnit.SECONDS)
    }
}
