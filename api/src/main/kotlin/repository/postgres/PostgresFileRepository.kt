package com.ctfp.repository.postgres

import com.ctfp.dto.File
import com.ctfp.repository.FileRepository


class PostgresFileRepository : FileRepository {
    override suspend fun getFile(id: Int): File {
        TODO("Not yet implemented")
    }

    override suspend fun getAllFiles(): List<File> {
        TODO("Not yet implemented")
    }

    override suspend fun createFile(file: File): Int {
        TODO("Not yet implemented")
    }

    override suspend fun updateFile(id: Int, file: File) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteFile(id: Int) {
        TODO("Not yet implemented")
    }
}
