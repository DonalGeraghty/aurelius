package com.donalgeraghty.stoicwidget

object MessageText {
    const val MAX_LENGTH = 500

    fun normalize(value: String): String? =
        value.trim().takeIf { it.isNotEmpty() && it.length <= MAX_LENGTH }
}
