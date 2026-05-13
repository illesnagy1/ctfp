package com.ctfp.domain.dao

import com.ctfp.domain.model.SubmissionTable
import com.ctfp.dto.Submission
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class SubmissionDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SubmissionDAO>(SubmissionTable)

    var userId by SubmissionTable.userId
    var teamId by SubmissionTable.teamId
    var challengeId by SubmissionTable.challengeId
    var flagId by SubmissionTable.flagId
    var submittedAt by SubmissionTable.submittedAt

    fun toModel() = Submission(
        userId = userId.value,
        teamId = teamId?.value,
        challengeId = challengeId.value,
        flagId = flagId?.value,
        submittedAt = submittedAt,
    )
}
