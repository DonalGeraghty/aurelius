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
        fun resolve(context: Context): WidgetAppearance {
            val preferences = WidgetPreferences(context)
            val theme = effectiveTheme(context, preferences.theme)
            val transparent = preferences.transparentBackground

            val backgroundResource = when {
                transparent -> R.drawable.widget_background_transparent
                theme == WidgetPreferences.Theme.LIGHT -> R.drawable.widget_background_light
                theme == WidgetPreferences.Theme.WALLPAPER -> R.drawable.widget_background_dynamic
                else -> R.drawable.widget_background
            }

            val customColor = when (theme) {
                WidgetPreferences.Theme.LIGHT -> preferences.lightTextColor
                WidgetPreferences.Theme.DARK -> preferences.darkTextColor
                else -> null
            }
            val quoteColor = when (theme) {
                WidgetPreferences.Theme.LIGHT -> customColor
                    ?: context.getColor(R.color.widget_text_light)
                WidgetPreferences.Theme.WALLPAPER -> context.getColor(R.color.widget_dynamic_text)
                else -> customColor
                    ?: context.getColor(R.color.widget_text_dark)
            }
            val authorColor = when (theme) {
                WidgetPreferences.Theme.WALLPAPER -> context.getColor(R.color.widget_dynamic_secondary)
                WidgetPreferences.Theme.LIGHT -> customColor?.let(::secondaryColor)
                    ?: context.getColor(R.color.widget_secondary_light)
                else -> customColor?.let(::secondaryColor)
                    ?: context.getColor(R.color.widget_secondary_dark)
            }

            return WidgetAppearance(
                backgroundResource = backgroundResource,
                quoteColor = quoteColor,
                authorColor = authorColor,
                fontScale = preferences.fontSize.scale,
                showAttribution = preferences.showAttribution,
            )
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
