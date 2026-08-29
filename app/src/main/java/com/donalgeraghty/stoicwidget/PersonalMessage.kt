package com.donalgeraghty.stoicwidget

data class PersonalMessage(
    val id: String,
    val text: String,
    val createdAt: Long,
    val source: String? = null,
    val collection: String = MessageText.DEFAULT_COLLECTION,
    val enabled: Boolean = true,
)
