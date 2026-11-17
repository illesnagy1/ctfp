package com.ctfp.controller.helper

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header

// Optimistic concurrency (RFC 9110 §13.1.1): reads send the record's version as ETag, writes may send it back as
// If-Match, and a write to a record changed since is refused with 412 (see UserService.lockUser). Writes don't answer
// with the new ETag: ConditionalHeaders would check If-Match against it and refuse every write; read it again instead.

/** The If-Match header, if the client sent one. */
val ApplicationCall.ifMatch: String? get() = request.header(HttpHeaders.IfMatch)
