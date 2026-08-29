package com.donalgeraghty.stoicwidget

import android.content.Context

class WidgetPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    var contentMode: ContentMode
        get() = ContentMode.fromStoredValue(preferences.getString(KEY_CONTENT_MODE, null))
        set(value) = preferences.edit().putString(KEY_CONTENT_MODE, value.name).apply()

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

    var lightTextColor: Int?
        get() = storedColor(KEY_LIGHT_TEXT_COLOR)
        set(value) = storeColor(KEY_LIGHT_TEXT_COLOR, value)

    var darkTextColor: Int?
        get() = storedColor(KEY_DARK_TEXT_COLOR)
        set(value) = storeColor(KEY_DARK_TEXT_COLOR, value)

    var selectedCollection: String?
        get() = preferences.getString(KEY_SELECTED_COLLECTION, null)?.takeIf { it.isNotBlank() }
        set(value) = preferences.edit().apply {
            if (value.isNullOrBlank()) remove(KEY_SELECTED_COLLECTION)
            else putString(KEY_SELECTED_COLLECTION, value)
        }.apply()

    fun resetTextColors() {
        preferences.edit()
            .remove(KEY_LIGHT_TEXT_COLOR)
            .remove(KEY_DARK_TEXT_COLOR)
            .apply()
    }

    private fun storedColor(key: String): Int? =
        if (preferences.contains(key)) preferences.getInt(key, 0) else null

    private fun storeColor(key: String, value: Int?) {
        preferences.edit().apply {
            if (value == null) remove(key) else putInt(key, value)
        }.apply()
    }

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
        private const val KEY_CONTENT_MODE = "content_mode"
        private const val KEY_THEME = "theme"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_TRANSPARENT_BACKGROUND = "transparent_background"
        private const val KEY_SHOW_ATTRIBUTION = "show_attribution"
        private const val KEY_LIGHT_TEXT_COLOR = "light_text_color"
        private const val KEY_DARK_TEXT_COLOR = "dark_text_color"
        private const val KEY_SELECTED_COLLECTION = "selected_collection"

        private inline fun <reified T : Enum<T>> enumValueOrDefault(
            storedValue: String?,
            defaultValue: T,
        ): T = enumValues<T>().firstOrNull { it.name == storedValue } ?: defaultValue
    }
}
