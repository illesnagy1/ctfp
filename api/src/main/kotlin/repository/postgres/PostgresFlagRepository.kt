package com.ctfp.repository.postgres

import com.ctfp.dto.Flag
import com.ctfp.repository.FlagRepository

class PostgresFlagRepository : FlagRepository {
    override suspend fun getFlag(id: Int): Flag {
        TODO("Not yet implemented")
    }

    override suspend fun getAllFlags(): List<Flag> {
        TODO("Not yet implemented")
    }

    override suspend fun createFlag(flag: Flag) {
        TODO("Not yet implemented")
    }

    override suspend fun updateFlag(id: Int, flag: Flag) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFlag(id: Int) {
        TODO("Not yet implemented")
    }
}
