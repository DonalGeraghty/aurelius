package com.donalgeraghty.stoicwidget

import android.content.Context

class WidgetPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    var theme: Theme
        get() = enumValueOrDefault(preferences.getString(KEY_THEME, null), Theme.DARK)
        set(value) = preferences.edit().putString(KEY_THEME, value.name).apply()

    var fontSize: FontSize
        get() = enumValueOrDefault(preferences.getString(KEY_FONT_SIZE, null), FontSize.MEDIUM)
        set(value) = preferences.edit().putString(KEY_FONT_SIZE, value.name).apply()

    var transparentBackground: Boolean
        get() = preferences.getBoolean(KEY_TRANSPARENT_BACKGROUND, false)
        set(value) = preferences.edit().putBoolean(KEY_TRANSPARENT_BACKGROUND, value).apply()

    var showAttribution: Boolean
        get() = preferences.getBoolean(KEY_SHOW_ATTRIBUTION, true)
        set(value) = preferences.edit().putBoolean(KEY_SHOW_ATTRIBUTION, value).apply()

    enum class Theme {
        SYSTEM,
        DARK,
        LIGHT,
        WALLPAPER,
    }

    enum class FontSize(val scale: Float) {
        SMALL(0.85f),
        MEDIUM(1f),
        LARGE(1.15f),
    }

    companion object {
        private const val PREFERENCES_NAME = "widget_appearance"
        private const val KEY_THEME = "theme"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_TRANSPARENT_BACKGROUND = "transparent_background"
        private const val KEY_SHOW_ATTRIBUTION = "show_attribution"

        private inline fun <reified T : Enum<T>> enumValueOrDefault(
            storedValue: String?,
            defaultValue: T,
        ): T = enumValues<T>().firstOrNull { it.name == storedValue } ?: defaultValue
    }
}
