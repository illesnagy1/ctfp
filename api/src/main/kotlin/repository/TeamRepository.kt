package com.ctfp.repository

import com.ctfp.dto.Team


interface TeamRepository {
    suspend fun getTeam(id: Int): Team
    suspend fun getAllTeams(): List<Team>
    suspend fun createTeam(team: Team): Int
    suspend fun updateTeam(id: Int, team: Team)
    suspend fun deleteTeam(id: Int)
}
