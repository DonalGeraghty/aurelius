package com.donalgeraghty.stoicwidget

enum class ContentMode {
    STOIC,
    PERSONAL;

    companion object {
        fun fromStoredValue(value: String?): ContentMode =
            values().firstOrNull { it.name == value } ?: STOIC
    }
}
