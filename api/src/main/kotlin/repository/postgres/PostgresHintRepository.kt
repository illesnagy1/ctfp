package com.ctfp.repository.postgres

import com.ctfp.dto.Hint
import com.ctfp.repository.HintRepository


class PostgresHintRepository : HintRepository {
    override suspend fun getHint(id: Int): Hint {
        TODO("Not yet implemented")
    }

    override suspend fun getAllHints(): List<Hint> {
        TODO("Not yet implemented")
    }

    override suspend fun createHint(template: Hint): Int {
        TODO("Not yet implemented")
    }

    override suspend fun updateHint(id: Int, hint: Hint) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteHint(id: Int) {
        TODO("Not yet implemented")
    }
}
