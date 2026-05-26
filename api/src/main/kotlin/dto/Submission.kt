package com.ctfp.dto

import com.ctfp.domain.dao.SubmissionDAO
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.dao.FlagDAO
import com.ctfp.domain.dao.TeamDAO
import kotlin.time.Instant
import kotlinx.serialization.Serializable

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
    user = UserDAO[dto.userId]
    team = dto.teamId?.let { TeamDAO[it] }
    challenge = ChallengeDAO[dto.challengeId]
    flag = dto.flagId?.let { FlagDAO[it] }
}

fun SubmissionDAO.toDto() = SubmissionResponse(
    id = id.value,
    userId = user.id.value,
    teamId = team?.id?.value,
    challengeId = challenge.id.value,
    flagId = flag?.id?.value,
    submittedAt = submittedAt
)
