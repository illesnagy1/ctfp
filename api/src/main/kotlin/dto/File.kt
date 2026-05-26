package com.ctfp.dto

import com.ctfp.domain.dao.FileDAO
import kotlinx.serialization.Serializable

@Serializable
data class FileRequest(
    val name: String
)

@Serializable
data class FileResponse(
    val id: Int,
    val name: String
)

fun FileDAO.apply(dto: FileRequest) {
    name = dto.name
}

fun FileDAO.toDto() = FileResponse(
    id = id.value,
    name = name
)
