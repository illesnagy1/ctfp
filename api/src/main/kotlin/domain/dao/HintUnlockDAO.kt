package com.ctfp.domain.dao

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.*

class HintUnlockTable : IntIdTable("hint_unlock") {
    companion object : IntEntityClass<HintUnlockDAO>(HintUnlockTable)

    var hintId by HintUnlockTable.hintId
    var userId by HintUnlockTable.userId
    var teamId by HintUnlockTable.teamId
    var unlockedAt by HintUnlockTable.unlockedAt
}
