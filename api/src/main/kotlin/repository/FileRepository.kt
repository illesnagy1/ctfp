package com.ctfp.repository

import com.ctfp.dto.File

interface FileRepository {
    suspend fun getFile(id: Int): File
    suspend fun getAllFiles(): List<File>
    suspend fun createFile(file: File): Int
    suspend fun updateFile(id: Int, file: File)
    suspend fun deleteFile(id: Int)
}