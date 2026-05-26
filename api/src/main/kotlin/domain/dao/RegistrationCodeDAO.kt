package com.ctfp.domain.dao

import com.ctfp.domain.model.RegistrationCodeTable
import com.ctfp.domain.model.UserTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class RegistrationCodeDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RegistrationCodeDAO>(RegistrationCodeTable)

    var code by RegistrationCodeTable.code
    var maxUses by RegistrationCodeTable.maxUses
    var createdAt by RegistrationCodeTable.createdAt
    var expiresAt by RegistrationCodeTable.expiresAt

    val usedByUsers by UserDAO referrersOn UserTable.codeId
}
