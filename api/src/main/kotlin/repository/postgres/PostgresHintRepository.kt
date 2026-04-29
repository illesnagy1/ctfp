package com.ctfp.repository.postgres

import com.ctfp.domain.dao.HintDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.HintTable
import com.ctfp.dto.Hint
import com.ctfp.repository.HintRepository


class PostgresHintRepository : HintRepository {
    override suspend fun getHint(id: Int): Hint = withTransaction {
        HintDAO[id].toModel()
    }

    override suspend fun getHintsForChallenge(challengeId: Int): List<Hint> = withTransaction {
        TODO()
    }

    override suspend fun getAllHints(): List<Hint> = withTransaction {
        HintDAO.all().map { it.toModel() }
    }

    override suspend fun createHint(template: Hint): Int = withTransaction {
        val newHint = HintDAO.new {
            challengeId = template.challengeId
            body = template.body
            cost = template.cost
            createdAt = template.createdAt
        }
        newHint.id.value
    }

    override suspend fun updateHint(id: Int, hint: Hint) = withTransaction {
        val dbHint = HintDAO[id]
        dbHint.run {
            challengeId = hint.challengeId
            body = hint.body
            cost = hint.cost
            createdAt = hint.createdAt
        }
    }

    override suspend fun deleteHint(id: Int) = withTransaction {
        HintDAO[id].delete()
    }
}
