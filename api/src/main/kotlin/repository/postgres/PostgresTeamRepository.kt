package com.ctfp.repository.postgres

import com.ctfp.dto.Team
import com.ctfp.repository.TeamRepository


class PostgresTeamRepository : TeamRepository {
    override suspend fun getTeam(id: Int): Team {
        TODO("Not yet implemented")
    }

    override suspend fun getAllTeams(): List<Team> {
        TODO("Not yet implemented")
    }

    override suspend fun createTeam(team: Team) {
        TODO("Not yet implemented")
    }

    override suspend fun updateTeam(team: Team) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTeam(team: Team) {
        TODO("Not yet implemented")
    }
}
