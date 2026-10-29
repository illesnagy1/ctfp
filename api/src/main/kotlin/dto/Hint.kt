package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import com.ctfp.domain.dao.HintDAO
import com.ctfp.domain.dao.ChallengeDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class HintRequest(
    val challengeId: Int,
    @JsonSchema.MinLength(1)
    val body: String,
    @JsonSchema.Minimum(0.0)
    val cost: Int = 0
)

@Serializable
data class HintResponse(
    val id: Int,
    val challengeId: Int,
    val body: String,
    val cost: Int = 0,
    val createdAt: Instant
)

fun HintDAO.apply(dto: HintRequest) {
    challenge = ChallengeDAO[dto.challengeId]
    body = dto.body
    cost = dto.cost
}

fun HintDAO.toDto() = HintResponse(
    id = id.value,
    challengeId = challenge.id.value,
    body = body,
    cost = cost,
    createdAt = createdAt
)
