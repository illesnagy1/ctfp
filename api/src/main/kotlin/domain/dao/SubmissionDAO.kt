package com.ctfp.domain.dao

import com.ctfp.domain.model.SubmissionTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class SubmissionDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SubmissionDAO>(SubmissionTable)

    var user by UserDAO referencedOn SubmissionTable.userId
    var team by TeamDAO optionalReferencedOn SubmissionTable.teamId
    var challenge by ChallengeDAO referencedOn SubmissionTable.challengeId
    var flag by FlagDAO optionalReferencedOn SubmissionTable.flagId
    var submittedAt by SubmissionTable.submittedAt
}
