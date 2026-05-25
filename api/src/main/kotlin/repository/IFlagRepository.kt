package com.ctfp.repository

import com.ctfp.dto.Flag

interface IFlagRepository {
    suspend fun getFlagForChallenge(challengeId: Int, id: Int): Flag
    suspend fun getFlagsForChallenge(challengeId: Int): List<Flag>
    suspend fun createFlag(challengeId: Int, flag: Flag): Int
    suspend fun updateFlag(challengeId: Int, id: Int, flag: Flag)
    suspend fun deleteFlag(id: Int)
}
