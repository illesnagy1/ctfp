package com.ctfp.service

import com.ctfp.domain.dao.PageDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.PageRequest
import com.ctfp.dto.PageResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto


class PageService {
    suspend fun getPage(id: Int): PageResponse = withTransaction {
        PageDAO[id].toDto()
    }

    suspend fun getAllPages(): List<PageResponse> = withTransaction {
        PageDAO.all().map { it.toDto() }
    }

    suspend fun createPage(page: PageRequest): Int = withTransaction {
        val newPage = PageDAO.new {
            apply(page)
        }
        newPage.id.value
    }

    suspend fun updatePage(id: Int, page: PageRequest) = withTransaction {
        PageDAO.findByIdAndUpdate(id) {
            it.apply(page)
        }
    }

    suspend fun deletePage(id: Int) = withTransaction {
        PageDAO[id].delete()
    }
}
