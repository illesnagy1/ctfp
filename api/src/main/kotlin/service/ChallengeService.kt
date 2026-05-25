package com.ctfp.service

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.dto.Challenge
import com.ctfp.domain.db.withTransaction


class ChallengeService {
    suspend fun getChallenge(id: Int): Challenge = withTransaction {
        ChallengeDAO[id].toModel()
    }

    suspend fun getAllChallenges(): List<Challenge> = withTransaction {
        ChallengeDAO.all().map { it.toModel() }
    }

    suspend fun createChallenge(challenge: Challenge): Int = withTransaction {
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

    suspend fun updateChallenge(id: Int, challenge: Challenge) = withTransaction {
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

    suspend fun deleteChallenge(id: Int) = withTransaction {
        ChallengeDAO[id].delete()
    }
}
