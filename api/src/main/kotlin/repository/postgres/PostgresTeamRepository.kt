package com.ctfp.repository.postgres

import com.ctfp.domain.dao.TeamDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.Team
import com.ctfp.repository.TeamRepository


class PostgresTeamRepository : TeamRepository {
    override suspend fun getTeam(id: Int): Team = withTransaction {
        TeamDAO[id].toModel()
    }

    override suspend fun getAllTeams(): List<Team> = withTransaction {
        TeamDAO.all().map { it.toModel() }
    }

    override suspend fun createTeam(team: Team) = withTransaction {
        TeamDAO.new {
            name = team.name
            ownerId = team.ownerId
            country = team.country
            website = team.website
            isHidden = team.isHidden
            isBanned = team.isBanned
            createdAt = team.createdAt
            updatedAt = team.updatedAt
        }
    }

    override suspend fun updateTeam(id: Int, team: Team) = withTransaction {
        val dbTeam = TeamDAO[id]
        dbTeam.run {
            name = team.name
            ownerId = team.ownerId
            country = team.country
            website = team.website
            isHidden = team.isHidden
            isBanned = team.isBanned
            createdAt = team.createdAt
            updatedAt = team.updatedAt
        }
    }

    override suspend fun deleteTeam(id: Int) = withTransaction {
        TeamDAO[id].delete()
    }
}
