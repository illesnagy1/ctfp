package com.ctfp.repository.postgres

import com.ctfp.domain.dao.RegistrationCodeDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.RegistrationCode
import com.ctfp.repository.RegistrationCodeRepository


class PostgresRegistrationCodeRepository : RegistrationCodeRepository {
    suspend fun getRegistrationCode(id: Int): RegistrationCode = withTransaction {
        RegistrationCodeDAO[id].toModel()
    }

    suspend fun getAllRegistrationCodes(): List<RegistrationCode> = withTransaction {
        RegistrationCodeDAO.all().map { it.toModel() }
    }

    suspend fun createRegistrationCode(regcode: RegistrationCode): Int = withTransaction {
        val newCode = RegistrationCodeDAO.new {
            code = regcode.code
            maxUses = regcode.maxUses
            createdAt = regcode.createdAt
            expiresAt = regcode.expiresAt
        }
        newCode.id.value
    }

    suspend fun updateRegistrationCode(id: Int, regcode: RegistrationCode) = withTransaction {
        val dbCode = RegistrationCodeDAO[id]
        dbCode.run {
            code = regcode.code
            maxUses = regcode.maxUses
            createdAt = regcode.createdAt
            expiresAt = regcode.expiresAt
        }
    }

    suspend fun deleteRegistrationCode(id: Int) = withTransaction {
        RegistrationCodeDAO[id].delete()
    }
}
