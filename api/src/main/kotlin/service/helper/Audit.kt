package com.ctfp.service.helper

import org.slf4j.LoggerFactory
import org.slf4j.event.Level

private val log = LoggerFactory.getLogger("audit")

private const val MAX_VALUE_LENGTH = 200

/**
 * Logs a security event on the `audit` logger, named from the OWASP Logging Vocabulary (e.g. `authn_login_fail`).
 * [details] become key/value pairs (fields in the JSON log format) and are in the message too; values are cut and
 * stripped of control characters, so user input can't forge log lines. The client IP comes from the MDC (Monitoring.kt).
 */
fun audit(
    event: String,
    vararg details: Pair<String, Any?>,
    level: Level = Level.INFO,
) {
    val clean = details.map { (key, value) -> key to value?.toString()?.replace(Regex("\\p{Cntrl}"), "_")?.take(MAX_VALUE_LENGTH) }
    val builder = log.atLevel(level).addKeyValue("event", event)
    clean.forEach { (key, value) -> builder.addKeyValue(key, value) }
    builder.log("{}:{}", event, clean.joinToString(",") { (key, value) -> "$key=$value" })
}
