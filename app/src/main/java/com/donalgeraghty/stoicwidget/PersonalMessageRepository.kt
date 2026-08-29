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
                                collection = MessageText.normalizeCollection(
                                    item.optString(KEY_COLLECTION),
                                ) ?: MessageText.DEFAULT_COLLECTION,
                                enabled = item.optBoolean(KEY_ENABLED, true),
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
        collection: String = MessageText.DEFAULT_COLLECTION,
    ): PersonalMessage? {
        val normalizedText = MessageText.normalize(text) ?: return null
        val normalizedCollection = MessageText.normalizeCollection(collection) ?: return null
        val message = PersonalMessage(
            id = UUID.randomUUID().toString(),
            text = normalizedText,
            source = MessageText.normalizeSource(source),
            collection = normalizedCollection,
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
        collection: String = MessageText.DEFAULT_COLLECTION,
    ): Boolean {
        val normalizedText = MessageText.normalize(text) ?: return false
        val normalizedCollection = MessageText.normalizeCollection(collection) ?: return false
        var changed = false
        val updatedMessages = all().map { message ->
            if (message.id == id) {
                changed = true
                message.copy(
                    text = normalizedText,
                    source = MessageText.normalizeSource(source),
                    collection = normalizedCollection,
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

    fun enabledCount(collection: String? = null): Int = all().count { message ->
        message.enabled && (collection == null || message.collection == collection)
    }

    fun collections(): List<String> = all().map { it.collection }.distinct().sorted()

    fun randomMessage(collection: String? = null): PersonalMessage? = all()
        .filter { message ->
            message.enabled && (collection == null || message.collection == collection)
        }
        .randomOrNull()

    @Synchronized
    fun duplicate(id: String): PersonalMessage? {
        val source = all().firstOrNull { it.id == id } ?: return null
        val duplicate = source.copy(
            id = UUID.randomUUID().toString(),
            createdAt = System.currentTimeMillis(),
        )
        val messages = all().toMutableList()
        messages.add(messages.indexOfFirst { it.id == id } + 1, duplicate)
        save(messages)
        return duplicate
    }

    @Synchronized
    fun setEnabled(id: String, enabled: Boolean): Boolean {
        var changed = false
        val messages = all().map { message ->
            if (message.id == id) {
                changed = message.enabled != enabled
                message.copy(enabled = enabled)
            } else {
                message
            }
        }
        if (changed) save(messages)
        return changed
    }

    @Synchronized
    fun moveBefore(movingId: String, targetId: String): Boolean {
        if (movingId == targetId) return false
        val messages = all().toMutableList()
        val movingIndex = messages.indexOfFirst { it.id == movingId }
        if (movingIndex < 0 || messages.none { it.id == targetId }) return false
        val moving = messages.removeAt(movingIndex)
        val targetIndex = messages.indexOfFirst { it.id == targetId }
        messages.add(targetIndex, moving)
        save(messages)
        return true
    }

    @Synchronized
    private fun save(messages: List<PersonalMessage>) {
        val array = JSONArray()
        messages.forEach { message ->
            array.put(
                JSONObject()
                    .put(KEY_ID, message.id)
                    .put(KEY_TEXT, message.text)
                    .put(KEY_SOURCE, message.source ?: "")
                    .put(KEY_COLLECTION, message.collection)
                    .put(KEY_ENABLED, message.enabled)
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
        private const val KEY_COLLECTION = "collection"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_CREATED_AT = "created_at"
    }
}
