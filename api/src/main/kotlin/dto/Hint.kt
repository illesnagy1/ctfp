package com.ctfp.dto

import com.ctfp.domain.dao.HintDAO
import com.ctfp.domain.dao.ChallengeDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class HintRequest(
    val challengeId: Int,
    val body: String,
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
