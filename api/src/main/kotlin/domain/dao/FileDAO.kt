package com.ctfp.domain.dao

import com.ctfp.domain.model.FileTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class FileDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FileDAO>(FileTable)

    var ownerType by FileTable.ownerType
    var ownerId by FileTable.ownerId
    var filename by FileTable.filename
    var storageKey by FileTable.storageKey
    var size by FileTable.size
    var mimeType by FileTable.mimeType
    var sha256 by FileTable.sha256
    val createdAt by FileTable.createdAt
}
