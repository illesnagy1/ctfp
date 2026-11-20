package com.ctfp.service.helper

import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.TeamDAO
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.model.PasswordResetTokenTable
import com.ctfp.domain.model.SubmissionTable
import com.ctfp.domain.model.TeamTable
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.etag
import com.ctfp.service.ApiException
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import java.security.MessageDigest

/** Must run inside a transaction. */
fun findUser(id: Int): UserDAO = UserDAO.findById(id) ?: throw ApiException(HttpStatusCode.NotFound, "User $id not found")

/**
 * [id]'s row, locked until the transaction ends, so writes to one account run one after another: a patch then applies
 * to the latest state, not one another write is replacing. With [ifMatch] (an If-Match header), 412 unless it names
 * the current version: the client edited what it saw, and someone has changed it since (RFC 9110 §13.1.1).
 */
fun lockUser(
    id: Int,
    ifMatch: String? = null,
): UserDAO {
    val user =
        UserDAO.find { UserTable.id eq id }.forUpdate().singleOrNull()
            ?: throw ApiException(HttpStatusCode.NotFound, "User $id not found")
    val current = etag(user.version)
    if (ifMatch != null && ifMatch.split(',').map(String::trim).none { it == "*" || it == current }) {
        throw ApiException(HttpStatusCode.PreconditionFailed, "This user was changed in the meantime; reload and try again")
    }
    return user
}

val activeAdmin get() = (UserTable.role eq UserRole.ADMIN) and (UserTable.isBanned eq false)

/** Must run inside a transaction. */
fun ensureAvailable(
    username: String,
    email: String,
    exceptId: Int? = null,
) {
    val clash = (UserTable.username eq username) or (UserTable.email eq email.lowercase())
    val query = if (exceptId == null) clash else clash and (UserTable.id neq exceptId)
    if (!UserDAO.find { query }.empty()) {
        throw ApiException(HttpStatusCode.Conflict, "Username or email is already taken")
    }
}

/** Hex SHA-256 of [secret]; random tokens and codes are stored this way, so a database leak doesn't hand them out. */
fun sha256Hex(secret: String): String =
    MessageDigest.getInstance("SHA-256").digest(secret.toByteArray()).joinToString("") { "%02x".format(it) }

/**
 * Deletes [user] with their submissions and reset tokens (the foreign keys don't cascade). 409 while they own a team,
 * so a team isn't left without an owner.
 */
fun deleteAccount(user: UserDAO) {
    if (!TeamDAO.find { TeamTable.ownerId eq user.id }.empty()) {
        throw ApiException(HttpStatusCode.Conflict, "${user.username} owns a team; delete it or change its owner first")
    }
    SubmissionTable.deleteWhere { SubmissionTable.userId eq user.id }
    PasswordResetTokenTable.deleteWhere { PasswordResetTokenTable.userId eq user.id }
    user.delete()
}

