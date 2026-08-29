package com.donalgeraghty.stoicwidget

object MessageText {
    const val MAX_LENGTH = 500
    const val SOURCE_MAX_LENGTH = 100
    const val COLLECTION_MAX_LENGTH = 40
    const val DEFAULT_COLLECTION = "General"

    fun normalize(value: String): String? =
        value.trim().takeIf { it.isNotEmpty() && it.length <= MAX_LENGTH }

    fun normalizeSource(value: String): String? = value
        .trim()
        .removePrefix("—")
        .removePrefix("-")
        .trim()
        .takeIf { it.isNotEmpty() && it.length <= SOURCE_MAX_LENGTH }

    fun normalizeCollection(value: String): String? = value
        .trim()
        .replace(Regex("\\s+"), " ")
        .takeIf { it.isNotEmpty() && it.length <= COLLECTION_MAX_LENGTH }

    fun titleCaseForDisplay(value: String): String {
        val result = StringBuilder(value.length)
        var capitalizeNextLetter = true
        value.forEach { character ->
            if (character.isLetter()) {
                result.append(if (capitalizeNextLetter) character.titlecaseChar() else character)
                capitalizeNextLetter = false
            } else {
                result.append(character)
                if (character.isWhitespace() || character in "-/–—") {
                    capitalizeNextLetter = true
                }
            }
        }
        return result.toString()
    }

    fun attributionForDisplay(source: String?): String? =
        source?.let(::normalizeSource)?.let { "— $it" }
}
