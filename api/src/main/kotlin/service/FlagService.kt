package com.ctfp.service

import com.ctfp.domain.dao.FlagDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.FlagRequest
import com.ctfp.dto.FlagResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto
import org.jetbrains.exposed.v1.core.eq


class FlagService {
    suspend fun getFlag(id: Int): FlagResponse = withTransaction {
        FlagDAO[id].toDto()
    }

    suspend fun getFlagForChallenge(challengeId: Int, id: Int): FlagResponse = withTransaction {
        FlagDAO[id].toDto()
    }

    suspend fun getFlagsForChallenge(challengeId: Int): List<FlagResponse> = withTransaction {
        FlagDAO.find { com.ctfp.domain.model.FlagTable.challengeId eq challengeId }.map { it.toDto() }
    }

    suspend fun createFlag(flag: FlagRequest): Int = withTransaction {
        val newFlag = FlagDAO.new {
            apply(flag)
        }
        newFlag.id.value
    }

    suspend fun updateFlag(challengeId: Int, id: Int, flag: FlagRequest) = withTransaction {
        FlagDAO.findByIdAndUpdate(id) {
            it.apply(flag)
        }
    }

    suspend fun deleteFlag(id: Int) = withTransaction {
        FlagDAO[id].delete()
    }
}
