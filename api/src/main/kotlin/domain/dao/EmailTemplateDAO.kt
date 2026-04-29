package com.ctfp.domain.dao

import com.ctfp.domain.model.EmailTemplateTable
import com.ctfp.dto.EmailTemplate
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class EmailTemplateDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EmailTemplateDAO>(EmailTemplateTable)

    var subject by EmailTemplateTable.subject
    var body by EmailTemplateTable.body
    var createdAt by EmailTemplateTable.createdAt
    var updatedAt by EmailTemplateTable.updatedAt

    fun toModel() = EmailTemplate(
        subject = subject,
        body = body,
    )
}
