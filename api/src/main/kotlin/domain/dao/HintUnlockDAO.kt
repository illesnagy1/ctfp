package com.ctfp.domain.dao

import com.ctfp.domain.model.HintUnlockTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class HintUnlockDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<HintUnlockDAO>(HintUnlockTable)

    var hint by HintDAO referencedOn HintUnlockTable.hintId
    var user by UserDAO referencedOn HintUnlockTable.userId
    var team by TeamDAO optionalReferencedOn HintUnlockTable.teamId
    var unlockedAt by HintUnlockTable.unlockedAt
}
