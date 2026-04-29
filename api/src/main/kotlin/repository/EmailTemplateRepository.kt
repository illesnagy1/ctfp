package com.ctfp.repository

import com.ctfp.dto.EmailTemplate


interface EmailTemplateRepository {
    suspend fun getEmailTemplate(id: Int): EmailTemplate
    suspend fun getAllEmailTemplates(): List<EmailTemplate>
    suspend fun createEmailTemplate(template: EmailTemplate): Int
    suspend fun updateEmailTemplate(id: Int, template: EmailTemplate)
    suspend fun deleteEmailTemplate(id: Int)
}
