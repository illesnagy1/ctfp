package com.ctfp.service

import com.ctfp.domain.dao.FileDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.FileRequest
import com.ctfp.dto.FileResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto


class FileService {
    suspend fun getFile(id: Int): FileResponse = withTransaction {
        FileDAO[id].toDto()
    }

    suspend fun getAllFiles(): List<FileResponse> = withTransaction {
        FileDAO.all().map { it.toDto() }
    }

    suspend fun createFile(file: FileRequest): Int = withTransaction {
        val newFile = FileDAO.new {
            apply(file)
        }
        newFile.id.value
    }

    suspend fun updateFile(id: Int, file: FileRequest) = withTransaction {
        FileDAO.findByIdAndUpdate(id) {
            it.apply(file)
        }
    }

    suspend fun deleteFile(id: Int) = withTransaction {
        FileDAO[id].delete()
    }
}
