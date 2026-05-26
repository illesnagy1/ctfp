package com.ctfp.service

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.ChallengeRequest
import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto


class ChallengeService {
    suspend fun getChallenge(id: Int): ChallengeResponse = withTransaction {
        ChallengeDAO[id].toDto()
    }

    suspend fun getAllChallenges(): List<ChallengeResponse> = withTransaction {
        ChallengeDAO.all().map { it.toDto() }
    }

    suspend fun createChallenge(challenge: ChallengeRequest): Int = withTransaction {
        val newChallenge = ChallengeDAO.new {
            apply(challenge)
        }
        newChallenge.id.value
    }

    suspend fun updateChallenge(id: Int, challenge: ChallengeRequest) = withTransaction {
        ChallengeDAO.findByIdAndUpdate(id) {
            it.apply(challenge)
        }
    }

    suspend fun deleteChallenge(id: Int) = withTransaction {
        ChallengeDAO[id].delete()
    }
}
