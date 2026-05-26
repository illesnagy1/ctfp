package com.ctfp.service

import com.ctfp.domain.dao.RegistrationCodeDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.RegistrationCodeRequest
import com.ctfp.dto.RegistrationCodeResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto


class RegistrationCodeService {
    suspend fun getRegistrationCode(id: Int): RegistrationCodeResponse = withTransaction {
        RegistrationCodeDAO[id].toDto()
    }

    suspend fun getAllRegistrationCodes(): List<RegistrationCodeResponse> = withTransaction {
        RegistrationCodeDAO.all().map { it.toDto() }
    }

    suspend fun createRegistrationCode(regcode: RegistrationCodeRequest): Int = withTransaction {
        val newRegistrationCode = RegistrationCodeDAO.new {
            apply(regcode)
        }
        newRegistrationCode.id.value
    }

    suspend fun updateRegistrationCode(id: Int, regcode: RegistrationCodeRequest) = withTransaction {
        RegistrationCodeDAO.findByIdAndUpdate(id) {
            it.apply(regcode)
        }
    }

    suspend fun deleteRegistrationCode(id: Int) = withTransaction {
        RegistrationCodeDAO[id].delete()
    }
}
