package com.ctfp.repository

import com.ctfp.dto.Hint


interface IHintRepository {
    suspend fun getHint(id: Int): Hint
    suspend fun getHintsForChallenge(challengeId: Int): List<Hint>
    suspend fun getAllHints(): List<Hint>
    suspend fun createHint(hint: Hint): Int
    suspend fun updateHint(id: Int, hint: Hint)
    suspend fun deleteHint(id: Int)
}
