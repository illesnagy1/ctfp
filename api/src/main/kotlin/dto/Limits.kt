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
    const val COUNTRY_PATTERN = "^[A-Z]{2}$"

    /**
     * A BCP 47 language tag (RFC 5646): language, then optional script, region and variants, e.g. `en`, `hu`, `pt-BR`,
     * `zh-Hant-TW`. Extensions and private use (`-u-…`, `-x-…`) aren't accepted.
     */
    const val LANGUAGE_TAG_PATTERN = "^[A-Za-z]{2,3}(-[A-Za-z]{4})?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*$"

    /** RFC 5646 §4.4.1: room for tags of at least 35 characters. */
    const val LANGUAGE_TAG_MAX = 35
    const val FILENAME_MAX = 255
    const val MIME_TYPE_MAX = 128
    const val IP_ADDRESS_MAX = 45
    const val USER_AGENT_MAX = 512
    const val VISIBILITY_SETTINGS_MAX = 4096
}
