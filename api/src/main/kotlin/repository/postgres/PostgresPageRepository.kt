package com.ctfp.repository.postgres

import com.ctfp.dto.Page
import com.ctfp.repository.PageRepository


class PostgresPageRepository : PageRepository {
    override suspend fun getPage(id: Int): Page {
        TODO("Not yet implemented")
    }

    override suspend fun getAllPages(): List<Page> {
        TODO("Not yet implemented")
    }

    override suspend fun createPage(page: Page): Int {
        TODO("Not yet implemented")
    }

    override suspend fun updatePage(id: Int, page: Page) {
        TODO("Not yet implemented")
    }

    override suspend fun deletePage(id: Int) {
        TODO("Not yet implemented")
    }
}
