package com.donalgeraghty.stoicwidget

import android.content.Context

data class WidgetContent(
    val text: String,
    val attribution: String?,
    val mode: ContentMode,
)

object WidgetContentSelector {
    fun random(context: Context): WidgetContent {
        val mode = WidgetPreferences(context).contentMode
        if (mode == ContentMode.PERSONAL) {
            val message = PersonalMessageRepository(context).randomMessage()
            if (message != null) {
                return WidgetContent(
                    text = message.text,
                    attribution = null,
                    mode = ContentMode.PERSONAL,
                )
            }
        }

        val quote = QuoteRepository.randomQuote()
        return WidgetContent(
            text = quote.text,
            attribution = quote.author,
            mode = ContentMode.STOIC,
        )
    }
}
