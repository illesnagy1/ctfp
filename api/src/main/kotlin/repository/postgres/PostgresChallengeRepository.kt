package com.ctfp.repository.postgres

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.dto.Challenge
import com.ctfp.repository.ChallengeRepository
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeTable
import org.jetbrains.exposed.v1.jdbc.selectAll


class PostgresChallengeRepository : ChallengeRepository {
    override suspend fun getChallenge(id: Int): Challenge = withTransaction {
        ChallengeDAO[id].toModel()
    }

    override suspend fun getAllChallenges(): List<Challenge> = withTransaction {
        ChallengeDAO.all().map { it.toModel() }
    }

    override suspend fun createChallenge(challenge: Challenge): Int = withTransaction {
        val newChallenge = ChallengeDAO.new {
            title = challenge.title
            description = challenge.description
            isVisible = challenge.isVisible
            publishAt = challenge.publishAt
            maxAttempts = challenge.maxAttempts
            categories = challenge.categories
            timeLimit = challenge.timeLimit
            createdAt = challenge.createdAt
            updatedAt = challenge.updatedAt
        }
        newChallenge.id.value
    }

    override suspend fun updateChallenge(id: Int, challenge: Challenge) = withTransaction {
        ChallengeDAO
        val dbChallenge = ChallengeDAO[id]
        dbChallenge.run {
            title = challenge.title
            description = challenge.description
            isVisible = challenge.isVisible
            publishAt = challenge.publishAt
            maxAttempts = challenge.maxAttempts
            categories = challenge.categories
            timeLimit = challenge.timeLimit
            createdAt = challenge.createdAt
            updatedAt = challenge.updatedAt
        }
    }

    override suspend fun deleteChallenge(id: Int) = withTransaction {
        ChallengeDAO[id].delete()
    }
}
