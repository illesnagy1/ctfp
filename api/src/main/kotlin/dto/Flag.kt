package com.ctfp.dto

import com.ctfp.domain.dao.FlagDAO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** How a flag's value drops as more teams solve it; stored lowercase in `flag.decay_fun`. */
@Serializable
enum class DecayFunction {
    @SerialName("static") STATIC,
    @SerialName("linear") LINEAR,
    @SerialName("logarithmic") LOGARITHMIC,
    @SerialName("parabolic") PARABOLIC,
}

/** A flag of the challenge in the path. */
@Serializable
data class FlagRequest(
    val pattern: String,
    val isCaseSensitive: Boolean = true,
    /** Points before any decay. */
    val baseValue: Int,
    val decayValue: Int? = null,
    val minValue: Int? = null,
    val decayFun: DecayFunction = DecayFunction.STATIC,
    val sortOrder: Int = 1,
)

@Serializable
data class FlagResponse(
    val id: Int,
    val challengeId: Int,
    val pattern: String,
    val isCaseSensitive: Boolean,
    val baseValue: Int,
    val decayValue: Int?,
    val minValue: Int?,
    val decayFun: DecayFunction,
    val sortOrder: Int,
    val createdAt: Instant,
)

/** Everything but the challenge, which comes from the path. */
fun FlagDAO.apply(dto: FlagRequest) {
    pattern = dto.pattern
    isCaseSensitive = dto.isCaseSensitive
    baseValue = dto.baseValue
    decayValue = dto.decayValue
    minValue = dto.minValue
    decayFun = dto.decayFun.name.lowercase()
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
    decayFun = DecayFunction.valueOf(decayFun.uppercase()),
    sortOrder = sortOrder,
    createdAt = createdAt,
)
