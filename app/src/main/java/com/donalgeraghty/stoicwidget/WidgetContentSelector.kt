package com.donalgeraghty.stoicwidget

import android.content.Context

data class WidgetContent(
    val text: String,
    val attribution: String?,
    val mode: ContentMode,
    val lightTextColor: String? = null,
    val darkTextColor: String? = null,
)

object WidgetContentFormatter {
    fun quote(content: WidgetContent): String {
        val text = if (content.mode == ContentMode.PERSONAL) {
            MessageText.titleCaseForDisplay(content.text)
        } else {
            content.text
        }
        return "\u201c$text\u201d"
    }

    fun attribution(content: WidgetContent): String? =
        if (content.mode == ContentMode.PERSONAL) {
            MessageText.attributionForDisplay(content.attribution)
        } else {
            content.attribution
        }
}

object WidgetContentSelector {
    fun random(context: Context): WidgetContent {
        val mode = WidgetPreferences(context).contentMode
        if (mode == ContentMode.PERSONAL) {
            val message = PersonalMessageRepository(context).randomMessage()
            if (message != null) {
                return WidgetContent(
                    text = message.text,
                    attribution = message.source,
                    mode = ContentMode.PERSONAL,
                    lightTextColor = message.lightTextColor,
                    darkTextColor = message.darkTextColor,
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
