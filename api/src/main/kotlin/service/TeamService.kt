package com.ctfp.service

import com.ctfp.domain.dao.TeamDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.TeamTable
import com.ctfp.dto.Team
import org.jetbrains.exposed.v1.core.dao.id.EntityID


class TeamService {
    suspend fun getTeam(id: Int): Team = withTransaction {
        TeamDAO[id].toModel()
    }

    suspend fun getAllTeams(): List<Team> = withTransaction {
        TeamDAO.all().map { it.toModel() }
    }

    suspend fun createTeam(team: Team) = withTransaction {
        val team = TeamDAO.new {
            name = team.name
            ownerId = EntityID(team.ownerId, TeamTable)
            country = team.country
            website = team.website
            isHidden = team.isHidden
            isBanned = team.isBanned
            createdAt = team.createdAt
            updatedAt = team.updatedAt
        }
        team.id.value
    }

    suspend fun updateTeam(id: Int, team: Team) = withTransaction {
        val dbTeam = TeamDAO[id]
        dbTeam.run {
            name = team.name
            ownerId = EntityID(team.ownerId, TeamTable)
            country = team.country
            website = team.website
            isHidden = team.isHidden
            isBanned = team.isBanned
            createdAt = team.createdAt
            updatedAt = team.updatedAt
        }
    }

    suspend fun deleteTeam(id: Int) = withTransaction {
        TeamDAO[id].delete()
    }
}
