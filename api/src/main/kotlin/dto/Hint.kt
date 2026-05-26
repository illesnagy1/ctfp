package com.ctfp.dto

import com.ctfp.domain.dao.HintDAO
import com.ctfp.domain.model.ChallengeTable
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID

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
    challengeId = EntityID(dto.challengeId, ChallengeTable)
    body = dto.body
    cost = dto.cost
}

fun HintDAO.toDto() = HintResponse(
    id = id.value,
    challengeId = challengeId.value,
    body = body,
    cost = cost,
    createdAt = createdAt
)
