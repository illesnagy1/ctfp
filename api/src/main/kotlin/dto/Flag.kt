package com.ctfp.dto

import com.ctfp.domain.dao.FlagDAO
import com.ctfp.domain.dao.ChallengeDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class FlagRequest(
    val challengeId: Int,
    val pattern: String,
    val isCaseSensitive: Boolean,
    val baseValue: Int,
    val decayValue: Int?,
    val minValue: Int? = null,
    val decayFun: String = "static",
    val sortOrder: Int = 1
)

@Serializable
data class FlagResponse(
    val id: Int,
    val challengeId: Int,
    val pattern: String,
    val isCaseSensitive: Boolean,
    val baseValue: Int,
    val decayValue: Int?,
    val minValue: Int? = null,
    val decayFun: String = "static",
    val sortOrder: Int = 1,
    val createdAt: Instant
)

fun FlagDAO.apply(dto: FlagRequest) {
    challenge = ChallengeDAO[dto.challengeId]
    pattern = dto.pattern
    isCaseSensitive = dto.isCaseSensitive
    baseValue = dto.baseValue
    decayValue = dto.decayValue
    minValue = dto.minValue
    decayFun = dto.decayFun
    sortOrder = dto.sortOrder
}

fun FlagDAO.toDto() = FlagResponse(
    id = id.value,
    challengeId = challenge.id.value,
    pattern = pattern,
    isCaseSensitive = isCaseSensitive,
    baseValue = baseValue,
    decayValue = decayValue,
    minValue = minValue,
    decayFun = decayFun,
    sortOrder = sortOrder,
    createdAt = createdAt
)
