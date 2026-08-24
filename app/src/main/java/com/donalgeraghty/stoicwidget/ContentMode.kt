package com.donalgeraghty.stoicwidget

enum class ContentMode {
    STOIC,
    PERSONAL;

    companion object {
        fun fromStoredValue(value: String?): ContentMode =
            entries.firstOrNull { it.name == value } ?: STOIC
    }
}
