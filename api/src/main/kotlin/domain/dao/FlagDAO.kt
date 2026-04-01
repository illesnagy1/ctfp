package com.ctfp.domain.dao

import com.ctfp.domain.model.FlagTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class FlagDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FlagDAO>(FlagTable)

    var challengeId by FlagTable.challengeId
    var pattern by FlagTable.pattern
    var isCaseSensitive by FlagTable.isCaseSensitive
    var baseValue by FlagTable.baseValue
    var decayValue by FlagTable.decayValue
    var minValue by FlagTable.minValue
    var decayFun by FlagTable.decayFun
    var sortOrder by FlagTable.sortOrder
    var createdAt by FlagTable.createdAt
}
