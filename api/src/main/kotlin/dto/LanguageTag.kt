package com.ctfp.dto

/**
 * [tag] in BCP 47's conventional case (RFC 5646 §2.1.1), e.g. `zh-hant-tw` → `zh-Hant-TW`, so the same language is
 * stored the same way however it was typed. Tags are case-insensitive; [Limits.LANGUAGE_TAG_PATTERN] checks the form.
 */
fun canonicalLanguageTag(tag: String): String =
    tag.split('-').withIndex().joinToString("-") { (i, subtag) ->
        when {
            i == 0 -> subtag.lowercase()
            subtag.length == 4 && subtag.all(Char::isLetter) -> subtag.lowercase().replaceFirstChar(Char::uppercase)
            subtag.length == 2 && subtag.all(Char::isLetter) -> subtag.uppercase()
            else -> subtag.lowercase()
        }
    }
