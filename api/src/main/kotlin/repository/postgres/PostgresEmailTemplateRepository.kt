package com.ctfp.repository.postgres

import com.ctfp.dto.EmailTemplate
import com.ctfp.repository.EmailTemplateRepository


class PostgresEmailTemplateRepository : EmailTemplateRepository {
    override suspend fun getEmailTemplate(id: String): EmailTemplate {
        TODO("Not yet implemented")
    }

    override suspend fun getAllEmailTemplates(): List<EmailTemplate> {
        TODO("Not yet implemented")
    }

    override suspend fun createEmailTemplate(template: EmailTemplate): Int {
        TODO("Not yet implemented")
    }

    override suspend fun updateEmailTemplate(id: Int, template: EmailTemplate) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEmailTemplate(id: Int) {
        TODO("Not yet implemented")
    }
}
