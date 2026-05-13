package com.ctfp.repository.postgres

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.dao.FlagDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeTable
import com.ctfp.dto.Flag
import com.ctfp.repository.FlagRepository
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.jdbc.insertAndGetId

class PostgresFlagRepository : FlagRepository {
    override suspend fun getFlagForChallenge(challengeId: Int, id: Int): Flag = withTransaction {
        FlagDAO[id].toModel()
    }

    override suspend fun getFlagsForChallenge(challengeId: Int): List<Flag> = withTransaction {
        TODO("Not yet implemented")
    }

    override suspend fun createFlag(challengeId: Int, flag: Flag): Int = withTransaction {
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
        newFlag.challengeId = EntityID(challengeId, ChallengeTable)
        newFlag.id.value
    }

    override suspend fun updateFlag(challengeId: Int, id: Int, flag: Flag) = withTransaction {
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
