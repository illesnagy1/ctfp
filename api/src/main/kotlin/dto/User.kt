package com.ctfp.dto

import io.ktor.openapi.JsonSchema
import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.UserDAO
import com.ctfp.domain.dao.RegistrationCodeDAO
import com.ctfp.domain.model.UserTable
import com.ctfp.service.ApiException
import io.ktor.http.HttpStatusCode
import kotlin.time.Instant
import kotlinx.serialization.Serializable

/** Fields a user may change on their own account. */
interface UserProfileFields {
    val username: String
    val email: String
    val language: String?
    val country: String?
    val website: String?
    val soundEnabled: Boolean
    val visibilitySettings: String?
}

/** Fields only an admin may change, on any account. */
interface AdminUserFields : UserProfileFields {
    val codeId: Int?
    val role: UserRole
    val isVerified: Boolean
    val isHidden: Boolean
    val isBanned: Boolean
}

@Serializable
data class UserUpdateRequest(
    @JsonSchema.Pattern(Limits.USERNAME_PATTERN)
    @JsonSchema.MinLength(Limits.USERNAME_MIN)
    @JsonSchema.MaxLength(Limits.USERNAME_MAX)
    override val username: String,
    @JsonSchema.Format("email")
    @JsonSchema.MaxLength(Limits.EMAIL_MAX)
    override val email: String,
    @JsonSchema.MaxLength(Limits.USER_LANGUAGE_MAX)
    override val language: String?,
    @JsonSchema.MinLength(Limits.COUNTRY_LENGTH)
    @JsonSchema.MaxLength(Limits.COUNTRY_LENGTH)
    @JsonSchema.Description("ISO 3166-1 alpha-2 code, e.g.: HU")
    override val country: String?,
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    override val website: String?,
    override val soundEnabled: Boolean = true,
    override val visibilitySettings: String?
) : UserProfileFields

@Serializable
data class AdminUserUpdateRequest(
    override val codeId: Int?,
    @JsonSchema.Pattern(Limits.USERNAME_PATTERN)
    @JsonSchema.MinLength(Limits.USERNAME_MIN)
    @JsonSchema.MaxLength(Limits.USERNAME_MAX)
    override val username: String,
    @JsonSchema.Format("email")
    @JsonSchema.MaxLength(Limits.EMAIL_MAX)
    override val email: String,
    @JsonSchema.MaxLength(Limits.USER_LANGUAGE_MAX)
    override val language: String?,
    @JsonSchema.MinLength(Limits.COUNTRY_LENGTH)
    @JsonSchema.MaxLength(Limits.COUNTRY_LENGTH)
    @JsonSchema.Description("ISO 3166-1 alpha-2 code, e.g. HU.")
    override val country: String?,
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    override val website: String?,
    override val role: UserRole,
    override val isVerified: Boolean = false,
    override val isHidden: Boolean = false,
    override val isBanned: Boolean = false,
    override val soundEnabled: Boolean = true,
    override val visibilitySettings: String?
) : AdminUserFields

/** Account created by an admin; the password is hashed server side. */
@Serializable
data class AdminUserCreateRequest(
    override val codeId: Int?,
    @JsonSchema.Pattern(Limits.USERNAME_PATTERN)
    @JsonSchema.MinLength(Limits.USERNAME_MIN)
    @JsonSchema.MaxLength(Limits.USERNAME_MAX)
    override val username: String,
    @JsonSchema.Format("email")
    @JsonSchema.MaxLength(Limits.EMAIL_MAX)
    override val email: String,
    @JsonSchema.MinLength(Limits.PASSWORD_MIN)
    @JsonSchema.MaxLength(Limits.PASSWORD_MAX)
    val password: String,
    @JsonSchema.MaxLength(Limits.USER_LANGUAGE_MAX)
    override val language: String?,
    @JsonSchema.MinLength(Limits.COUNTRY_LENGTH)
    @JsonSchema.MaxLength(Limits.COUNTRY_LENGTH)
    @JsonSchema.Description("ISO 3166-1 alpha-2 code, e.g. HU.")
    override val country: String?,
    @JsonSchema.MaxLength(Limits.SHORT_TEXT_MAX)
    override val website: String?,
    override val role: UserRole,
    override val isVerified: Boolean = false,
    override val isHidden: Boolean = false,
    override val isBanned: Boolean = false,
    override val soundEnabled: Boolean = true,
    override val visibilitySettings: String?
) : AdminUserFields

/** Public view of a user, safe to show to anyone. */
@Serializable
data class UserResponse(
    val id: Int,
    val username: String,
    val country: String?,
    val website: String?,
    val createdAt: Instant
)

/** Full view of a user, for admins and the user themselves. */
@Serializable
data class AdminUserResponse(
    val id: Int,
    val codeId: Int?,
    val username: String,
    val email: String,
    val language: String?,
    val country: String?,
    val website: String?,
    val role: UserRole,
    val isVerified: Boolean = false,
    val isHidden: Boolean = false,
    val isBanned: Boolean = false,
    val totpEnabled: Boolean = false,
    val soundEnabled: Boolean = true,
    val visibilitySettings: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

fun UserDAO.applyProfile(dto: UserProfileFields) {
    username = dto.username
    email = dto.email.lowercase()
    language = dto.language
    country = dto.country
    website = dto.website
    soundEnabled = dto.soundEnabled
    visibilitySettings = dto.visibilitySettings
}

/** Sets profile and admin fields; never touches [UserDAO.passwordHash] or TOTP. */
fun UserDAO.applyAdmin(dto: AdminUserFields) {
    applyProfile(dto)
    registrationCode = dto.codeId?.let {
        RegistrationCodeDAO.findById(it) ?: throw ApiException(HttpStatusCode.BadRequest, "Registration code $it not found")
    }
    role = dto.role
    isVerified = dto.isVerified
    isHidden = dto.isHidden
    isBanned = dto.isBanned
}

fun UserDAO.toDto() = UserResponse(
    id = id.value,
    username = username,
    country = country,
    website = website,
    createdAt = createdAt
)

fun UserDAO.toAdminDto() = AdminUserResponse(
    id = id.value,
    codeId = readValues[UserTable.codeId]?.value,
    username = username,
    email = email,
    language = language,
    country = country,
    website = website,
    role = role,
    isVerified = isVerified,
    isHidden = isHidden,
    isBanned = isBanned,
    totpEnabled = totpEnabled,
    soundEnabled = soundEnabled,
    visibilitySettings = visibilitySettings,
    createdAt = createdAt,
    updatedAt = updatedAt
)
