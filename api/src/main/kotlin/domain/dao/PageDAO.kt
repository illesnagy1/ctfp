package com.ctfp.domain.dao

import com.ctfp.domain.model.PageTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class PageDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PageDAO>(PageTable)

    var title by PageTable.title
    var route by PageTable.route
    var language by PageTable.language
    var format by PageTable.format
    var targetBlank by PageTable.targetBlank
    var body by PageTable.body
    var isHidden by PageTable.isHidden
    var isPrivate by PageTable.isPrivate
    var createdAt by PageTable.createdAt
    var updatedAt by PageTable.updatedAt
}
