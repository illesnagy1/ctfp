package com.ctfp

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeTable
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import org.jetbrains.exposed.v1.core.eq

/** A JSON request with an optional bearer token. */
suspend fun ApplicationTestBuilder.call(
    method: HttpMethod,
    path: String,
    token: String? = null,
    body: String? = null,
): HttpResponse =
    client.request(path) {
        this.method = method
        token?.let { bearerAuth(it) }
        if (body != null) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

/** A released challenge with one flag; returns its id. */
suspend fun ApplicationTestBuilder.challengeWithFlag(
    admin: String,
    title: String,
    flag: String,
    maxAttempts: Int = 0,
): Int {
    val created =
        call(
            HttpMethod.Post,
            "/v1/challenges",
            admin,
            """{"title":"$title","description":null,"publishAt":"2026-01-01T00:00:00Z","maxAttempts":$maxAttempts,""" +
                """"categories":[],"timeLimit":0}""",
        )
    check(created.status.value == 201) { "creating $title: ${created.status}" }
    val id = challengeId(title)
    val flagged = call(HttpMethod.Post, "/v1/challenges/$id/flags", admin, flag)
    check(flagged.status.value == 201) { "flag for $title: ${flagged.status}" }
    return id
}

suspend fun challengeId(title: String): Int =
    withTransaction {
        ChallengeDAO
            .find { ChallengeTable.title eq title }
            .single()
            .id.value
    }
