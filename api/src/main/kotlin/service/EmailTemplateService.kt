package com.ctfp.service

import com.ctfp.domain.dao.EmailTemplateDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.EmailTemplateRequest
import com.ctfp.dto.EmailTemplateResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto


class EmailTemplateService {
    suspend fun getEmailTemplate(id: Int): EmailTemplateResponse = withTransaction {
        EmailTemplateDAO[id].toDto()
    }

    suspend fun getAllEmailTemplates(): List<EmailTemplateResponse> = withTransaction {
        EmailTemplateDAO.all().map { it.toDto() }
    }

    suspend fun createEmailTemplate(template: EmailTemplateRequest): Int = withTransaction {
        val newTemplate = EmailTemplateDAO.new {
            apply(template)
        }
        newTemplate.id.value
    }

    suspend fun updateEmailTemplate(id: Int, template: EmailTemplateRequest) = withTransaction {
        EmailTemplateDAO.findByIdAndUpdate(id) {
            it.apply(template)
        }
    }

    suspend fun deleteEmailTemplate(id: Int) = withTransaction {
        EmailTemplateDAO[id].delete()
    }
}
