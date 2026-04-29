package com.ctfp.repository

import com.ctfp.dto.Challenge
import com.ctfp.dto.Flag


interface ChallengeRepository {
    suspend fun getChallenge(id: Int): Challenge
    suspend fun getAllChallenges(): List<Challenge>
    suspend fun createChallenge(challenge: Challenge): Int
    suspend fun updateChallenge(id: Int, challenge: Challenge)
    suspend fun deleteChallenge(id: Int)
}
