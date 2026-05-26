package com.ctfp.dto

import com.ctfp.domain.dao.SubmissionDAO
import com.ctfp.domain.model.ChallengeTable
import com.ctfp.domain.model.FlagTable
import com.ctfp.domain.model.TeamTable
import com.ctfp.domain.model.UserTable
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.EntityID

@Serializable
data class SubmissionRequest(
    val userId: Int,
    val teamId: Int?,
    val challengeId: Int,
    val flagId: Int?
)

@Serializable
data class SubmissionResponse(
    val id: Int,
    val userId: Int,
    val teamId: Int?,
    val challengeId: Int,
    val flagId: Int?,
    val submittedAt: Instant
)

fun SubmissionDAO.apply(dto: SubmissionRequest) {
    userId = EntityID(dto.userId, UserTable)
    teamId = dto.teamId?.let { EntityID(it, TeamTable) }
    challengeId = EntityID(dto.challengeId, ChallengeTable)
    flagId = dto.flagId?.let { EntityID(it, FlagTable) }
}

fun SubmissionDAO.toDto() = SubmissionResponse(
    id = id.value,
    userId = userId.value,
    teamId = teamId?.value,
    challengeId = challengeId.value,
    flagId = flagId?.value,
    submittedAt = submittedAt
)
