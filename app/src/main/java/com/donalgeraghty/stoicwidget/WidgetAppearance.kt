package com.donalgeraghty.stoicwidget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build

data class WidgetAppearance(
    val backgroundResource: Int,
    val quoteColor: Int,
    val authorColor: Int,
    val fontScale: Float,
    val showAttribution: Boolean,
) {
    companion object {
        fun resolve(context: Context, content: WidgetContent? = null): WidgetAppearance {
            val preferences = WidgetPreferences(context)
            val theme = effectiveTheme(context, preferences.theme)
            val transparent = preferences.transparentBackground

            val backgroundResource = when {
                transparent -> R.drawable.widget_background_transparent
                theme == WidgetPreferences.Theme.LIGHT -> R.drawable.widget_background_light
                theme == WidgetPreferences.Theme.WALLPAPER -> R.drawable.widget_background_dynamic
                else -> R.drawable.widget_background
            }

            val defaultQuoteColor = when (theme) {
                WidgetPreferences.Theme.LIGHT -> context.getColor(R.color.widget_text_light)
                WidgetPreferences.Theme.WALLPAPER -> context.getColor(R.color.widget_dynamic_text)
                else -> context.getColor(R.color.widget_text_dark)
            }
            val defaultAuthorColor = when (theme) {
                WidgetPreferences.Theme.LIGHT -> context.getColor(R.color.widget_secondary_light)
                WidgetPreferences.Theme.WALLPAPER -> context.getColor(R.color.widget_dynamic_secondary)
                else -> context.getColor(R.color.widget_secondary_dark)
            }
            val customColor = when (theme) {
                WidgetPreferences.Theme.LIGHT -> content?.lightTextColor
                WidgetPreferences.Theme.DARK -> content?.darkTextColor
                else -> null
            }?.let(::parseColorOrNull)
            val quoteColor = customColor ?: defaultQuoteColor
            val authorColor = customColor?.let(::secondaryColor) ?: defaultAuthorColor

            return WidgetAppearance(
                backgroundResource = backgroundResource,
                quoteColor = quoteColor,
                authorColor = authorColor,
                fontScale = preferences.fontSize.scale,
                showAttribution = preferences.showAttribution,
            )
        }

        private fun parseColorOrNull(value: String): Int? =
            try {
                Color.parseColor(value)
            } catch (_: IllegalArgumentException) {
                null
            }

        private fun secondaryColor(color: Int): Int {
            val alpha = (Color.alpha(color) * 0.75f).toInt()
            return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        }

        private fun effectiveTheme(
            context: Context,
            selectedTheme: WidgetPreferences.Theme,
        ): WidgetPreferences.Theme {
            if (selectedTheme == WidgetPreferences.Theme.WALLPAPER && Build.VERSION.SDK_INT < 31) {
                return systemTheme(context)
            }
            return if (selectedTheme == WidgetPreferences.Theme.SYSTEM) {
                systemTheme(context)
            } else {
                selectedTheme
            }
        }

        private fun systemTheme(context: Context): WidgetPreferences.Theme {
            val nightMode = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
            return if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
                WidgetPreferences.Theme.DARK
            } else {
                WidgetPreferences.Theme.LIGHT
            }
        }
    }
}
