package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object FileTable : IntIdTable() {
    val name = varchar("name", 255)
}