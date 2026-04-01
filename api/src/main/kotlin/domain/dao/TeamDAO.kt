package com.ctfp.domain.dao

import com.ctfp.domain.model.TeamTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class TeamDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TeamDAO>(TeamTable)

    var name by TeamTable.name
    var ownerId by TeamTable.ownerId
    var country by TeamTable.country
    var website by TeamTable.website
    var isHidden by TeamTable.isHidden
    var isBanned by TeamTable.isBanned
    var createdAt by TeamTable.createdAt
    var updatedAt by TeamTable.updatedAt
}
