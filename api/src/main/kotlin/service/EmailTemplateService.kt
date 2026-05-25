package com.ctfp.service

import com.ctfp.domain.dao.EmailTemplateDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.EmailTemplate


class EmailTemplateService {
    suspend fun getEmailTemplate(id: Int): EmailTemplate = withTransaction {
        EmailTemplateDAO[id].toModel()
    }

    suspend fun getAllEmailTemplates(): List<EmailTemplate> = withTransaction {
        EmailTemplateDAO.all().map { it.toModel() }
    }

    suspend fun createEmailTemplate(template: EmailTemplate): Int = withTransaction {
        val newTemplate = EmailTemplateDAO.new {
            subject = template.subject
            body = template.body
        }
        newTemplate.id.value
    }

    suspend fun updateEmailTemplate(id: Int, template: EmailTemplate) = withTransaction {
        val dbTemplate = EmailTemplateDAO[id]
        dbTemplate.run {
            subject = template.subject
            body = template.body
        }
    }

    suspend fun deleteEmailTemplate(id: Int) = withTransaction {
        EmailTemplateDAO[id].delete()
    }
}
