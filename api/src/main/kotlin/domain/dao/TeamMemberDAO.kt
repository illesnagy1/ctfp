package com.ctfp.domain.dao

import com.ctfp.domain.model.TeamMemberTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class TeamMemberDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TeamMemberDAO>(TeamMemberTable)

    var team by TeamDAO referencedOn TeamMemberTable.teamId
    var user by UserDAO referencedOn TeamMemberTable.userId
    var joinedAt by TeamMemberTable.joinedAt
}
