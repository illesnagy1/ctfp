package com.ctfp.domain.model

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

/** Metadata of uploaded files; the contents live in [com.ctfp.storage.FileStorage] under [storageKey]. */
object FileTable : IntIdTable() {
    /** What the file belongs to, e.g. [FileOwner.CHALLENGE]. No foreign key, so owners delete their files themselves. */
    val ownerType = varchar("owner_type", 64)
    val ownerId = integer("owner_id")
    /** Original name, only used for display and the download's file name. */
    val filename = varchar("filename", 255)
    val storageKey = varchar("storage_key", 64).uniqueIndex()
    val size = long("size")
    val mimeType = varchar("mime_type", 128).nullable()
    val sha256 = varchar("sha256", 64)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        index("idx_files_owner", false, ownerType, ownerId)
    }
}

object FileOwner {
    const val CHALLENGE = "challenge"
}
