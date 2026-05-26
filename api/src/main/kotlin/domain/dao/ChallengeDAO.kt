package com.ctfp.domain.dao

import com.ctfp.domain.model.ChallengeTable
import com.ctfp.domain.model.HintTable
import com.ctfp.domain.model.FlagTable
import com.ctfp.domain.model.SubmissionTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class ChallengeDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ChallengeDAO>(ChallengeTable)

    var title by ChallengeTable.title
    var description by ChallengeTable.description
    var isVisible by ChallengeTable.isVisible
    var publishAt by ChallengeTable.publishAt
    var maxAttempts by ChallengeTable.maxAttempts
    var categories by ChallengeTable.categories
    var timeLimit by ChallengeTable.timeLimit
    var createdAt by ChallengeTable.createdAt
    var updatedAt by ChallengeTable.updatedAt

    val hints by HintDAO referrersOn HintTable.challengeId
    val flags by FlagDAO referrersOn FlagTable.challengeId
    val submissions by SubmissionDAO referrersOn SubmissionTable.challengeId
}
