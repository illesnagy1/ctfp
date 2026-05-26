package com.ctfp.service

import com.ctfp.domain.dao.HintDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.HintRequest
import com.ctfp.dto.HintResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto
import org.jetbrains.exposed.v1.core.eq


class HintService {
    suspend fun getHint(id: Int): HintResponse = withTransaction {
        HintDAO[id].toDto()
    }

    suspend fun getHintsForChallenge(challengeId: Int): List<HintResponse> = withTransaction {
        HintDAO.find { com.ctfp.domain.model.HintTable.challengeId eq challengeId }.map { it.toDto() }
    }

    suspend fun getAllHints(): List<HintResponse> = withTransaction {
        HintDAO.all().map { it.toDto() }
    }

    suspend fun createHint(hint: HintRequest): Int = withTransaction {
        val newHint = HintDAO.new {
            apply(hint)
        }
        newHint.id.value
    }

    suspend fun updateHint(id: Int, hint: HintRequest) = withTransaction {
        HintDAO.findByIdAndUpdate(id) {
            it.apply(hint)
        }
    }

    suspend fun deleteHint(id: Int) = withTransaction {
        HintDAO[id].delete()
    }
}