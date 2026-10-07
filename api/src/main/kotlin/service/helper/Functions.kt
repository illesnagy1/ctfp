package com.ctfp.service.helper

import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.UserProfileFields
import com.ctfp.service.ApiException
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.or

/** Must run inside a transaction. */
fun findUser(id: Int): UserDAO =
    UserDAO.findById(id) ?: throw ApiException(HttpStatusCode.NotFound, "User $id not found")

fun validate(user: UserProfileFields) {
    validateUsername(user.username)
    validateEmail(user.email)
}

/** Must run inside a transaction. */
fun ensureAvailable(username: String, email: String, exceptId: Int? = null) {
    val clash = (UserTable.username eq username) or (UserTable.email eq email.lowercase())
    val query = if (exceptId == null) clash else clash and (UserTable.id neq exceptId)
    if (!UserDAO.find { query }.empty()) {
        throw ApiException(HttpStatusCode.Conflict, "Username or email is already taken")
    }
}