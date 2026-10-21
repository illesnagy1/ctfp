package com.ctfp.service

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeTable
import com.ctfp.dto.ChallengeRequest
import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import kotlin.time.Clock


class ChallengeService : DefaultService<Int, ChallengeDAO, ChallengeRequest, ChallengeResponse>(
    dao = ChallengeDAO,
    table = ChallengeTable,
    apply = ChallengeDAO::apply,
    toDto = ChallengeDAO::toDto
) {
    /** Challenges players may see: visible and already published. */
    private fun released(): Op<Boolean> =
        (ChallengeTable.isVisible eq true) and (ChallengeTable.publishAt lessEq Clock.System.now())

    /** All challenges for admins, only released ones for everyone else. */
    suspend fun getAll(includeUnreleased: Boolean): List<ChallengeResponse> =
        if (includeUnreleased) getAll() else withTransaction { ChallengeDAO.find(released()).map { it.toDto() } }

    /** Unreleased challenges are a 404 for non-admins, the same as missing ones, so their ids don't leak. */
    suspend fun get(id: Int, includeUnreleased: Boolean): ChallengeResponse =
        if (includeUnreleased) get(id) else withTransaction {
            ChallengeDAO.find { (ChallengeTable.id eq id) and released() }.firstOrNull()?.toDto()
                ?: throw ApiException(HttpStatusCode.NotFound, "Not found")
        }

    fun getAllFlags(id: Int) = ChallengeDAO[id].flags
    fun getAllSubmissions(id: Int) = ChallengeDAO[id].submissions
    fun getAllHints(id: Int) = ChallengeDAO[id].hints
}
