package com.ctfp.domain.dao

import com.ctfp.domain.model.UserTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class UserDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDAO>(UserTable)

    var codeId by UserTable.codeId
    var username by UserTable.username
    var email by UserTable.email
    var passwordHash by UserTable.passwordHash
    var language by UserTable.language
    var country by UserTable.country
    var website by UserTable.website
    var role by UserTable.role
    var isVerified by UserTable.isVerified
    var isHidden by UserTable.isHidden
    var isBanned by UserTable.isBanned
    var totpSecret by UserTable.totpSecret
    var totpEnabled by UserTable.totpEnabled
    var soundEnabled by UserTable.soundEnabled
    var visibilitySettings by UserTable.visibilitySettings
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt
}
