package com.ctfp.service

import com.ctfp.domain.dao.PageDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.Page


class PageService {
    suspend fun getPage(id: Int): Page = withTransaction {
        PageDAO[id].toModel()
    }

    suspend fun getAllPages(): List<Page> = withTransaction {
        PageDAO.all().map { it.toModel() }
    }

    suspend fun createPage(page: Page): Int = withTransaction {
        val newPage = PageDAO.new {
            title = page.title
            route = page.route
            language = page.language
            format = page.format
            targetBlank = page.targetBlank
            body = page.body
            isHidden = page.isHidden
            isPrivate = page.isPrivate
            createdAt = page.createdAt
            updatedAt = page.updatedAt
        }
        newPage.id.value
    }

    suspend fun updatePage(id: Int, page: Page) = withTransaction {
        val dbPage = PageDAO[id]
        dbPage.run {
            title = page.title
            route = page.route
            language = page.language
            format = page.format
            targetBlank = page.targetBlank
            body = page.body
            isHidden = page.isHidden
            isPrivate = page.isPrivate
            createdAt = page.createdAt
            updatedAt = page.updatedAt
        }
    }

    suspend fun deletePage(id: Int) = withTransaction {
        PageDAO[id].delete()
    }
}
