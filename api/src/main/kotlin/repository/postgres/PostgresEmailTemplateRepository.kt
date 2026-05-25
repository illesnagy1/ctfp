package com.ctfp.repository.postgres

import com.ctfp.domain.dao.EmailTemplateDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.EmailTemplate
import com.ctfp.repository.IEmailTemplateRepository


class PostgresEmailTemplateRepository : IEmailTemplateRepository {
    override suspend fun getEmailTemplate(id: Int): EmailTemplate = withTransaction {
        EmailTemplateDAO[id].toModel()
    }

    override suspend fun getAllEmailTemplates(): List<EmailTemplate> = withTransaction {
        EmailTemplateDAO.all().map { it.toModel() }
    }

    override suspend fun createEmailTemplate(template: EmailTemplate): Int = withTransaction {
        val newTemplate = EmailTemplateDAO.new {
            subject = template.subject
            body = template.body
        }
        newTemplate.id.value
    }

    override suspend fun updateEmailTemplate(id: Int, template: EmailTemplate) = withTransaction {
        val dbTemplate = EmailTemplateDAO[id]
        dbTemplate.run {
            subject = template.subject
            body = template.body
        }
    }

    override suspend fun deleteEmailTemplate(id: Int) = withTransaction {
        EmailTemplateDAO[id].delete()
    }
}
