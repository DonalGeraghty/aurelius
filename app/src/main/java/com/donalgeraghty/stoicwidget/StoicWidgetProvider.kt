package com.donalgeraghty.stoicwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews

class StoicWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_RANDOMIZE_QUOTE) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            )
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateWidget(
                    context,
                    AppWidgetManager.getInstance(context),
                    appWidgetId,
                    selectNewQuote = true,
                )
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidget(
            context,
            appWidgetManager,
            appWidgetId,
            selectNewQuote = false,
        )
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        preferences.edit().apply {
            appWidgetIds.forEach { appWidgetId ->
                remove(quoteTextKey(appWidgetId))
                remove(quoteAuthorKey(appWidgetId))
            }
        }.apply()
    }

    companion object {
        private const val ACTION_RANDOMIZE_QUOTE =
            "com.donalgeraghty.stoicwidget.action.RANDOMIZE_QUOTE"
        private const val PREFERENCES_NAME = "stoic_widget_quotes"
        private const val QUOTE_TEXT_KEY_PREFIX = "quote_text_"
        private const val QUOTE_AUTHOR_KEY_PREFIX = "quote_author_"

        fun updateAllWidgets(
            context: Context,
            selectNewQuote: Boolean = true,
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, StoicWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { id ->
                updateWidget(context, manager, id, selectNewQuote)
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            appWidgetId: Int,
            selectNewQuote: Boolean = true,
        ) {
            val quote = quoteForWidget(context, appWidgetId, selectNewQuote)
            val layoutResource = layoutForWidget(manager, appWidgetId)
            val appearance = WidgetAppearance.resolve(context)
            val views = RemoteViews(
                context.packageName,
                layoutResource,
            ).apply {
                setTextViewText(R.id.quoteText, "“${quote.text}”")
                setTextViewText(R.id.quoteAuthor, quote.author)
                setInt(R.id.widgetRoot, "setBackgroundResource", appearance.backgroundResource)
                setTextColor(R.id.quoteText, appearance.quoteColor)
                setTextColor(R.id.quoteAuthor, appearance.authorColor)
                setTextViewTextSize(
                    R.id.quoteText,
                    TypedValue.COMPLEX_UNIT_SP,
                    quoteTextSize(layoutResource) * appearance.fontScale,
                )
                setTextViewTextSize(
                    R.id.quoteAuthor,
                    TypedValue.COMPLEX_UNIT_SP,
                    authorTextSize(layoutResource) * appearance.fontScale,
                )
                setViewVisibility(
                    R.id.quoteAuthor,
                    if (appearance.showAttribution) View.VISIBLE else View.GONE,
                )

                val openAppIntent = Intent(context, MainActivity::class.java)
                val openAppPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                setOnClickPendingIntent(R.id.widgetRoot, openAppPendingIntent)

                val randomizeIntent = Intent(context, StoicWidgetProvider::class.java).apply {
                    action = ACTION_RANDOMIZE_QUOTE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                val randomizePendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    randomizeIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                setOnClickPendingIntent(R.id.quoteText, randomizePendingIntent)
                setOnClickPendingIntent(R.id.quoteAuthor, randomizePendingIntent)
            }
            manager.updateAppWidget(appWidgetId, views)
        }

        private fun quoteTextSize(layoutResource: Int): Float = when (layoutResource) {
            R.layout.widget_stoic_compact -> 14f
            R.layout.widget_stoic_large -> 22f
            else -> 18f
        }

        private fun authorTextSize(layoutResource: Int): Float = when (layoutResource) {
            R.layout.widget_stoic_compact -> 10f
            R.layout.widget_stoic_large -> 15f
            else -> 13f
        }

        private fun quoteForWidget(
            context: Context,
            appWidgetId: Int,
            selectNewQuote: Boolean,
        ): Quote {
            val preferences = context.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE,
            )
            if (!selectNewQuote) {
                val text = preferences.getString(quoteTextKey(appWidgetId), null)
                val author = preferences.getString(quoteAuthorKey(appWidgetId), null)
                if (text != null && author != null) {
                    return Quote(text, author)
                }
            }

            return QuoteRepository.randomQuote().also { quote ->
                preferences.edit()
                    .putString(quoteTextKey(appWidgetId), quote.text)
                    .putString(quoteAuthorKey(appWidgetId), quote.author)
                    .apply()
            }
        }

        private fun quoteTextKey(appWidgetId: Int): String =
            "$QUOTE_TEXT_KEY_PREFIX$appWidgetId"

        private fun quoteAuthorKey(appWidgetId: Int): String =
            "$QUOTE_AUTHOR_KEY_PREFIX$appWidgetId"

        private fun layoutForWidget(
            manager: AppWidgetManager,
            appWidgetId: Int,
        ): Int {
            val options = manager.getAppWidgetOptions(appWidgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

            return when {
                minWidth >= 250 && minHeight >= 180 -> R.layout.widget_stoic_large
                minWidth < 180 -> R.layout.widget_stoic_compact
                else -> R.layout.widget_stoic
            }
        }
    }
}
