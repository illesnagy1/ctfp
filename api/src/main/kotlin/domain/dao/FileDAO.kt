package com.ctfp.domain.dao

import com.ctfp.domain.model.FileTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class FileDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FileDAO>(FileTable)

    var name by FileTable.name
}
