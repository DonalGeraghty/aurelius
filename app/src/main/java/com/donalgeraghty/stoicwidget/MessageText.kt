package com.donalgeraghty.stoicwidget

object MessageText {
    const val MAX_LENGTH = 500
    const val SOURCE_MAX_LENGTH = 100

    private val colorPattern = Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$")

    fun normalize(value: String): String? =
        value.trim().takeIf { it.isNotEmpty() && it.length <= MAX_LENGTH }

    fun normalizeSource(value: String): String? = value
        .trim()
        .removePrefix("—")
        .removePrefix("-")
        .trim()
        .takeIf { it.isNotEmpty() && it.length <= SOURCE_MAX_LENGTH }

    fun normalizeColor(value: String): String? = value
        .trim()
        .takeIf { colorPattern.matches(it) }
        ?.uppercase()

    fun isValidOptionalColor(value: String): Boolean =
        value.isBlank() || normalizeColor(value) != null

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
