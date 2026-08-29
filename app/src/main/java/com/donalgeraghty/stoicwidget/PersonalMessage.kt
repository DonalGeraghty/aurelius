package com.donalgeraghty.stoicwidget

data class PersonalMessage(
    val id: String,
    val text: String,
    val createdAt: Long,
    val source: String? = null,
    val lightTextColor: String? = null,
    val darkTextColor: String? = null,
)
