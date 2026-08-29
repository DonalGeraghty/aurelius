package com.donalgeraghty.stoicwidget

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

class PersonalMessageRepository(context: Context) {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    @Synchronized
    fun all(): List<PersonalMessage> {
        val storedMessages = preferences.getString(KEY_MESSAGES, null) ?: return emptyList()
        return try {
            val array = JSONArray(storedMessages)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val id = item.optString(KEY_ID).trim()
                    val text = MessageText.normalize(item.optString(KEY_TEXT)) ?: continue
                    if (id.isNotEmpty()) {
                        add(
                            PersonalMessage(
                                id = id,
                                text = text,
                                source = MessageText.normalizeSource(item.optString(KEY_SOURCE)),
                                lightTextColor = MessageText.normalizeColor(
                                    item.optString(KEY_LIGHT_TEXT_COLOR),
                                ),
                                darkTextColor = MessageText.normalizeColor(
                                    item.optString(KEY_DARK_TEXT_COLOR),
                                ),
                                createdAt = item.optLong(KEY_CREATED_AT, 0L),
                            ),
                        )
                    }
                }
            }
        } catch (_: JSONException) {
            emptyList()
        }
    }

    @Synchronized
    fun add(
        text: String,
        source: String = "",
        lightTextColor: String = "",
        darkTextColor: String = "",
    ): PersonalMessage? {
        val normalizedText = MessageText.normalize(text) ?: return null
        val message = PersonalMessage(
            id = UUID.randomUUID().toString(),
            text = normalizedText,
            source = MessageText.normalizeSource(source),
            lightTextColor = MessageText.normalizeColor(lightTextColor),
            darkTextColor = MessageText.normalizeColor(darkTextColor),
            createdAt = System.currentTimeMillis(),
        )
        save(all() + message)
        return message
    }

    @Synchronized
    fun update(
        id: String,
        text: String,
        source: String = "",
        lightTextColor: String = "",
        darkTextColor: String = "",
    ): Boolean {
        val normalizedText = MessageText.normalize(text) ?: return false
        var changed = false
        val updatedMessages = all().map { message ->
            if (message.id == id) {
                changed = true
                message.copy(
                    text = normalizedText,
                    source = MessageText.normalizeSource(source),
                    lightTextColor = MessageText.normalizeColor(lightTextColor),
                    darkTextColor = MessageText.normalizeColor(darkTextColor),
                )
            } else {
                message
            }
        }
        if (changed) save(updatedMessages)
        return changed
    }

    @Synchronized
    fun delete(id: String): Boolean {
        val messages = all()
        val remainingMessages = messages.filterNot { it.id == id }
        if (remainingMessages.size == messages.size) return false
        save(remainingMessages)
        return true
    }

    fun count(): Int = all().size

    fun randomMessage(): PersonalMessage? = all().randomOrNull()

    @Synchronized
    private fun save(messages: List<PersonalMessage>) {
        val array = JSONArray()
        messages.forEach { message ->
            array.put(
                JSONObject()
                    .put(KEY_ID, message.id)
                    .put(KEY_TEXT, message.text)
                    .put(KEY_SOURCE, message.source ?: "")
                    .put(KEY_LIGHT_TEXT_COLOR, message.lightTextColor ?: "")
                    .put(KEY_DARK_TEXT_COLOR, message.darkTextColor ?: "")
                    .put(KEY_CREATED_AT, message.createdAt),
            )
        }
        preferences.edit().putString(KEY_MESSAGES, array.toString()).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "personal_messages"
        private const val KEY_MESSAGES = "personal_messages_v1"
        private const val KEY_ID = "id"
        private const val KEY_TEXT = "text"
        private const val KEY_SOURCE = "source"
        private const val KEY_LIGHT_TEXT_COLOR = "light_text_color"
        private const val KEY_DARK_TEXT_COLOR = "dark_text_color"
        private const val KEY_CREATED_AT = "created_at"
    }
}
