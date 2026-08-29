package com.donalgeraghty.stoicwidget

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class CustomCardsActivity : Activity() {
    private lateinit var repository: PersonalMessageRepository
    private lateinit var preferences: WidgetPreferences
    private lateinit var cardsContainer: LinearLayout
    private lateinit var resultCount: TextView
    private lateinit var collectionFilter: Spinner
    private var searchQuery = ""
    private var filteredCollection: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_cards)
        repository = PersonalMessageRepository(this)
        preferences = WidgetPreferences(this)
        cardsContainer = findViewById(R.id.cardsContainer)
        resultCount = findViewById(R.id.cardResultCount)
        collectionFilter = findViewById(R.id.cardCollectionFilter)

        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }
        findViewById<Button>(R.id.addCardButton).setOnClickListener { showCardDialog(null) }
        findViewById<EditText>(R.id.cardSearchInput).addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(value: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(value: CharSequence?, start: Int, before: Int, count: Int) {
                    searchQuery = value?.toString().orEmpty().trim()
                    renderCards()
                }
                override fun afterTextChanged(value: Editable?) = Unit
            },
        )
        configureCollectionFilter()
        renderCards()
    }

    private fun configureCollectionFilter() {
        val options = listOf(getString(R.string.all_collections)) + repository.collections()
        val position = filteredCollection?.let(options::indexOf)?.takeIf { it >= 0 } ?: 0
        if (position == 0) filteredCollection = null
        collectionFilter.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options,
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        collectionFilter.setSelection(position)
        collectionFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            private var ignoreInitialSelection = true

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (ignoreInitialSelection) {
                    ignoreInitialSelection = false
                    return
                }
                filteredCollection = options.getOrNull(position)?.takeIf { position > 0 }
                renderCards()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun renderCards() {
        val cards = repository.all().filter { card ->
            (filteredCollection == null || card.collection == filteredCollection) &&
                (searchQuery.isBlank() ||
                    card.text.contains(searchQuery, ignoreCase = true) ||
                    card.source.orEmpty().contains(searchQuery, ignoreCase = true) ||
                    card.collection.contains(searchQuery, ignoreCase = true))
        }
        resultCount.text = resources.getQuantityString(R.plurals.card_result_count, cards.size, cards.size)
        cardsContainer.removeAllViews()
        if (cards.isEmpty()) {
            TextView(this).apply {
                setText(if (repository.count() == 0) R.string.no_personal_messages else R.string.no_matching_cards)
                setPadding(0, 24, 0, 24)
                cardsContainer.addView(this)
            }
            return
        }
        cards.forEach { card -> cardsContainer.addView(createCardRow(card)) }
    }

    private fun createCardRow(card: PersonalMessage): View {
        val row = LayoutInflater.from(this).inflate(R.layout.item_personal_message, cardsContainer, false)
        row.alpha = if (card.enabled) 1f else 0.55f
        row.findViewById<TextView>(R.id.messageText).text = MessageText.titleCaseForDisplay(card.text)
        row.findViewById<TextView>(R.id.messageSource).apply {
            text = MessageText.attributionForDisplay(card.source).orEmpty()
            visibility = if (card.source == null) View.GONE else View.VISIBLE
        }
        row.findViewById<TextView>(R.id.messageCollection).text = card.collection
        row.findViewById<Switch>(R.id.messageEnabledSwitch).apply {
            isChecked = card.enabled
            setOnCheckedChangeListener { _, checked ->
                if (repository.setEnabled(card.id, checked)) contentChanged()
            }
        }
        row.findViewById<Button>(R.id.editMessageButton).setOnClickListener { showCardDialog(card) }
        row.findViewById<Button>(R.id.duplicateMessageButton).setOnClickListener {
            repository.duplicate(card.id)
            contentChanged()
        }
        row.findViewById<Button>(R.id.deleteMessageButton).setOnClickListener { confirmDelete(card) }
        row.setOnLongClickListener {
            it.startDragAndDrop(
                ClipData.newPlainText(DRAG_LABEL, card.id),
                View.DragShadowBuilder(it),
                null,
                0,
            )
            true
        }
        row.setOnDragListener { _, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> event.clipDescription?.label == DRAG_LABEL
                DragEvent.ACTION_DROP -> {
                    val movingId = event.clipData?.getItemAt(0)?.text?.toString().orEmpty()
                    if (repository.moveBefore(movingId, card.id)) contentChanged()
                    true
                }
                else -> true
            }
        }
        return row
    }

    private fun showCardDialog(card: PersonalMessage?) {
        val content = LayoutInflater.from(this).inflate(R.layout.dialog_personal_message, null)
        val text = content.findViewById<EditText>(R.id.dialogMessageInput).apply {
            setText(card?.text.orEmpty())
            filters = arrayOf(InputFilter.LengthFilter(MessageText.MAX_LENGTH))
        }
        val source = content.findViewById<EditText>(R.id.dialogSourceInput).apply {
            setText(card?.source.orEmpty())
            filters = arrayOf(InputFilter.LengthFilter(MessageText.SOURCE_MAX_LENGTH))
        }
        val collection = content.findViewById<EditText>(R.id.dialogCollectionInput).apply {
            setText(card?.collection ?: filteredCollection ?: MessageText.DEFAULT_COLLECTION)
            filters = arrayOf(InputFilter.LengthFilter(MessageText.COLLECTION_MAX_LENGTH))
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(if (card == null) R.string.add_card else R.string.edit_card)
            .setView(content)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val success = if (card == null) {
                    repository.add(
                        text.text.toString(),
                        source.text.toString(),
                        collection.text.toString(),
                    ) != null
                } else {
                    repository.update(
                        card.id,
                        text.text.toString(),
                        source.text.toString(),
                        collection.text.toString(),
                    )
                }
                if (success) {
                    dialog.dismiss()
                    contentChanged()
                } else {
                    Toast.makeText(this, R.string.card_fields_required, Toast.LENGTH_LONG).show()
                }
            }
        }
        dialog.show()
    }

    private fun confirmDelete(card: PersonalMessage) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_message)
            .setMessage(R.string.delete_message_confirmation)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                if (repository.delete(card.id)) contentChanged()
            }
            .show()
    }

    private fun contentChanged() {
        val collections = repository.collections()
        preferences.selectedCollection?.let { selected ->
            if (selected !in collections) preferences.selectedCollection = null
        }
        preferences.selectedCollection?.let { selected ->
            if (repository.enabledCount(selected) == 0) preferences.selectedCollection = null
        }
        if (repository.enabledCount() == 0 && preferences.contentMode == ContentMode.PERSONAL) {
            preferences.contentMode = ContentMode.STOIC
            Toast.makeText(this, R.string.returned_to_stoic_mode, Toast.LENGTH_LONG).show()
        }
        StoicWidgetProvider.updateAllWidgets(this, selectNewContent = true)
        configureCollectionFilter()
        renderCards()
    }

    companion object {
        private const val DRAG_LABEL = "aurelius_card_id"
    }
}
