package com.ctfp.repository.postgres

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.dto.Challenge
import com.ctfp.repository.ChallengeRepository
import com.ctfp.domain.db.withTransaction


class PostgresChallengeRepository : ChallengeRepository {
    override suspend fun getChallenge(id: Int) = withTransaction {
        ChallengeDAO[id].toModel()
    }

    override suspend fun getAllChallenges(): List<Challenge> = withTransaction {
        ChallengeDAO.all().map { it.toModel() }
    }

    override suspend fun createChallenge(challenge: Challenge): Int = withTransaction {
        val newChallenge = ChallengeDAO.new {
            title = challenge.title
            categories = challenge.categories
            description = challenge.description
            TODO()
        }
        newChallenge.id.value
    }

    override suspend fun updateChallenge(id: Int, challenge: Challenge) = withTransaction {
        val dbChallenge = ChallengeDAO[id]
        dbChallenge.run {
            title = challenge.title
            categories = challenge.categories
            description = challenge.description
        }
    }

    override suspend fun deleteChallenge(id: Int) = withTransaction {
        ChallengeDAO[id].delete()
    }
}
