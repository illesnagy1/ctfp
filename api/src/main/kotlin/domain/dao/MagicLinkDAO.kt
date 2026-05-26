package com.ctfp.domain.dao

import com.ctfp.domain.model.MagicLinkTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class MagicLinkDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<MagicLinkDAO>(MagicLinkTable)

    var userId by MagicLinkTable.userId
    var token by MagicLinkTable.token
    var createdAt by MagicLinkTable.createdAt
    var expiresAt by MagicLinkTable.expiresAt
    var usedAt by MagicLinkTable.usedAt
}
