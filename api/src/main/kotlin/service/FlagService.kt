package com.ctfp.service

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.dao.FlagDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.FlagTable
import com.ctfp.dto.DecayFunction
import com.ctfp.dto.FlagRequest
import com.ctfp.dto.FlagResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.eq
import java.util.regex.PatternSyntaxException

/** Flags are the answers, so every method here is for admins only (see the routes). */
class FlagService {
    suspend fun getForChallenge(challengeId: Int): List<FlagResponse> = withTransaction {
        findChallenge(challengeId)
        FlagDAO.find { FlagTable.challengeId eq challengeId }
            .sortedWith(compareBy({ it.sortOrder }, { it.id.value }))
            .map { it.toDto() }
    }

    suspend fun create(challengeId: Int, flag: FlagRequest): FlagResponse {
        validate(flag)
        return withTransaction {
            val owner = findChallenge(challengeId)
            FlagDAO.new {
                challenge = owner
                apply(flag)
            }.toDto()
        }
    }

    suspend fun update(id: Int, flag: FlagRequest) {
        validate(flag)
        withTransaction { findFlag(id).apply(flag) }
    }

    suspend fun delete(id: Int) = withTransaction {
        findFlag(id).delete()
    }

    private fun findChallenge(id: Int) =
        ChallengeDAO.findById(id) ?: throw ApiException(HttpStatusCode.NotFound, "Challenge $id not found")

    private fun findFlag(id: Int) =
        FlagDAO.findById(id) ?: throw ApiException(HttpStatusCode.NotFound, "Flag $id not found")

    private fun validate(flag: FlagRequest) {
        fun invalid(message: String): Nothing = throw ApiException(HttpStatusCode.BadRequest, message)

        if (flag.pattern.isBlank()) invalid("Pattern is required")
        if (flag.pattern.length > 255) invalid("Pattern must be at most 255 characters")
        try {
            Regex(flag.pattern)
        } catch (e: PatternSyntaxException) {
            invalid("Pattern is not a valid regular expression: ${e.description}")
        }
        if (flag.baseValue < 0) invalid("Value must be 0 or more")
        if (flag.decayFun != DecayFunction.STATIC) {
            val decay = flag.decayValue ?: invalid("Decay is required unless the value is static")
            val min = flag.minValue ?: invalid("Minimum value is required unless the value is static")
            if (decay < 0) invalid("Decay must be 0 or more")
            if (min < 0 || min > flag.baseValue) invalid("Minimum value must be between 0 and the value")
        }
    }
}
