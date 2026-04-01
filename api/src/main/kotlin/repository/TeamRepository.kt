package com.ctfp.repository

import com.ctfp.dto.Team


interface TeamRepository {
    suspend fun getTeam(id: Int): Team
    suspend fun getAllTeams(): List<Team>
    suspend fun createTeam(team: Team)
    suspend fun updateTeam(team: Team)
    suspend fun deleteTeam(team: Team)
}
