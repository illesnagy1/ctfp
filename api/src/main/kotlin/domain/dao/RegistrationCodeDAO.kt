package com.ctfp.domain.dao

import com.ctfp.domain.model.RegistrationCodeTable
import com.ctfp.dto.RegistrationCode
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class RegistrationCodeDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RegistrationCodeDAO>(RegistrationCodeTable)

    var code by RegistrationCodeTable.code
    var maxUses by RegistrationCodeTable.maxUses
    var createdAt by RegistrationCodeTable.createdAt
    var expiresAt by RegistrationCodeTable.expiresAt

    fun toModel() = RegistrationCode(
        code = code,
        maxUses = maxUses,
        createdAt = createdAt,
        expiresAt = expiresAt,
    )
}
