package com.ctfp.service

import com.ctfp.domain.dao.TeamDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.TeamRequest
import com.ctfp.dto.TeamResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto


class TeamService {
    suspend fun getTeam(id: Int): TeamResponse = withTransaction {
        TeamDAO[id].toDto()
    }

    suspend fun getAllTeams(): List<TeamResponse> = withTransaction {
        TeamDAO.all().map { it.toDto() }
    }

    suspend fun createTeam(team: TeamRequest): Int = withTransaction {
        val newTeam = TeamDAO.new {
            apply(team)
        }
        newTeam.id.value
    }

    suspend fun updateTeam(id: Int, team: TeamRequest) = withTransaction {
        TeamDAO.findByIdAndUpdate(id) {
            it.apply(team)
        }
    }

    suspend fun deleteTeam(id: Int) = withTransaction {
        TeamDAO[id].delete()
    }
}
