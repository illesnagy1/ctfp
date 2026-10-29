package com.ctfp.dto

/**
 * Input limits, shared by the request DTOs' `@JsonSchema` annotations (which put them into openapi.json, and from
 * there into the frontend) and the services' checks. Annotations only document, so a limit is only enforced where a
 * service checks it, or where the database column is that size.
 */
object Limits {
    // UserValidation.kt
    const val USERNAME_PATTERN = "^[A-Za-z0-9._-]{3,64}$"
    const val USERNAME_MIN = 3
    const val USERNAME_MAX = 64
    const val EMAIL_MAX = 255
    const val PASSWORD_MIN = 8
    const val PASSWORD_MAX = 128

    // FlagService.kt, SubmissionService.kt
    const val FLAG_PATTERN_MAX = 255
    const val SUBMITTED_FLAG_MAX = 1024

    // Column sizes (domain/model)
    const val TITLE_MAX = 255
    const val SHORT_TEXT_MAX = 255
    const val TEAM_NAME_MAX = 64
    const val COUNTRY_LENGTH = 2
    const val USER_LANGUAGE_MAX = 5
    const val PAGE_LANGUAGE_LENGTH = 2
}
