package com.ctfp.repository

import com.ctfp.dto.Hint
import com.ctfp.dto.Page


interface PageRepository {
    suspend fun getPage(id: Int): Page
    suspend fun getAllPages(): List<Page>
    suspend fun createPage(page: Page): Int
    suspend fun updatePage(id: Int, page: Page)
    suspend fun deletePage(id: Int)
}
