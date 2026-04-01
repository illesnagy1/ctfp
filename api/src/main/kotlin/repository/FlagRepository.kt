package com.ctfp.repository

import com.ctfp.dto.Flag

interface FlagRepository {
    suspend fun getFlag(id: Int): Flag
    suspend fun getAllFlags(): List<Flag>
    suspend fun createFlag(flag: Flag)
    suspend fun updateFlag(id: Int, flag: Flag)
    suspend fun deleteFlag(id: Int)
}
