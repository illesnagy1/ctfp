package com.ctfp.domain.dao

import com.ctfp.domain.model.HintTable
import com.ctfp.domain.model.HintUnlockTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class HintDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HintDAO>(HintTable)

    var challenge by ChallengeDAO referencedOn HintTable.challengeId
    var body by HintTable.body
    var cost by HintTable.cost
    var createdAt by HintTable.createdAt

    val unlockedBy by HintUnlockDAO referrersOn HintUnlockTable.hintId
}
