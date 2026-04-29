package com.ctfp.domain.dao

import com.ctfp.domain.model.TeamMemberTable
import com.ctfp.dto.TeamMember
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class TeamMemberDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TeamMemberDAO>(TeamMemberTable)

    var teamId by TeamMemberTable.teamId
    var userId by TeamMemberTable.userId
    var joinedAt by TeamMemberTable.joinedAt

    fun toModel() = TeamMember(
        teamId = teamId.value,
        userId = userId.value,
        joinedAt = joinedAt,
    )
}
