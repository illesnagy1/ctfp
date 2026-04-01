package com.ctfp.domain.dao

import com.ctfp.domain.model.PasswordResetTokenTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class PasswordResetTokenDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PasswordResetTokenDAO>(PasswordResetTokenTable)

    var userId by PasswordResetTokenTable.userId
    var token by PasswordResetTokenTable.token
    var createdAt by PasswordResetTokenTable.createdAt
    var expiresAt by PasswordResetTokenTable.expiresAt
    var usedAt by PasswordResetTokenTable.usedAt
}
