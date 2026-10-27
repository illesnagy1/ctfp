package com.ctfp.dto

import com.ctfp.domain.dao.FileDAO
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/** An uploaded file's metadata; download it from `/v1/files/{id}/download`. */
@Serializable
data class FileResponse(
    val id: Int,
    val filename: String,
    val size: Long,
    val mimeType: String?,
    /** Hex SHA-256 of the contents, for players to verify their download. */
    val sha256: String,
    val createdAt: Instant,
)

fun FileDAO.toDto() = FileResponse(
    id = id.value,
    filename = filename,
    size = size,
    mimeType = mimeType,
    sha256 = sha256,
    createdAt = createdAt,
)
