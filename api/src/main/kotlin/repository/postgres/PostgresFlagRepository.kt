package com.ctfp.repository.postgres

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.dao.FlagDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.Flag
import com.ctfp.repository.FlagRepository

class PostgresFlagRepository : FlagRepository {
    override suspend fun getFlag(id: Int): Flag = withTransaction {
        FlagDAO[id].toModel()
    }

    override suspend fun getFlagsForChallenge(challengeId: Int): List<Flag> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllFlags(): List<Flag> = withTransaction {
        FlagDAO.all().map { it.toModel() }
    }

    override suspend fun createFlag(flag: Flag): Int = withTransaction {
        val newFlag = FlagDAO.new {
            pattern = flag.pattern
            isCaseSensitive = flag.isCaseSensitive
            baseValue = flag.baseValue
            decayValue = flag.decayValue
            minValue = flag.minValue
            decayFun = flag.decayFun
            sortOrder = flag.sortOrder
            createdAt = flag.createdAt
        }
        newFlag.id.value
    }

    override suspend fun updateFlag(id: Int, flag: Flag) = withTransaction {
        val dbFlag = FlagDAO[id]
        dbFlag.run {
            pattern = flag.pattern
            isCaseSensitive = flag.isCaseSensitive
            baseValue = flag.baseValue
            decayValue = flag.decayValue
            minValue = flag.minValue
            decayFun = flag.decayFun
            sortOrder = flag.sortOrder
            createdAt = flag.createdAt
        }
    }

    override suspend fun deleteFlag(id: Int) = withTransaction {
        FlagDAO[id].delete()
    }
}
