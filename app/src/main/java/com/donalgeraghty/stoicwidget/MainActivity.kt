package com.donalgeraghty.stoicwidget

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputFilter
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

class MainActivity : Activity() {
    private lateinit var quoteText: TextView
    private lateinit var quoteAuthor: TextView
    private lateinit var contentCount: TextView
    private lateinit var previewCard: LinearLayout
    private lateinit var personalModeSwitch: Switch
    private lateinit var modeStatus: TextView
    private lateinit var messageInput: EditText
    private lateinit var sourceInput: EditText
    private lateinit var lightColorInput: EditText
    private lateinit var darkColorInput: EditText
    private lateinit var personalMessageCount: TextView
    private lateinit var messagesContainer: LinearLayout
    private lateinit var attributionSwitch: Switch
    private lateinit var preferences: WidgetPreferences
    private lateinit var messageRepository: PersonalMessageRepository
    private lateinit var previewContent: WidgetContent
    private var settingUpControls = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        quoteText = findViewById(R.id.previewQuote)
        quoteAuthor = findViewById(R.id.previewAuthor)
        contentCount = findViewById(R.id.contentCount)
        previewCard = findViewById(R.id.previewCard)
        personalModeSwitch = findViewById(R.id.personalModeSwitch)
        modeStatus = findViewById(R.id.modeStatus)
        messageInput = findViewById(R.id.messageInput)
        sourceInput = findViewById(R.id.sourceInput)
        lightColorInput = findViewById(R.id.lightColorInput)
        darkColorInput = findViewById(R.id.darkColorInput)
        personalMessageCount = findViewById(R.id.personalMessageCount)
        messagesContainer = findViewById(R.id.messagesContainer)
        attributionSwitch = findViewById(R.id.showAttributionSwitch)
        preferences = WidgetPreferences(this)
        messageRepository = PersonalMessageRepository(this)
        if (
            preferences.contentMode == ContentMode.PERSONAL &&
            messageRepository.count() == 0
        ) {
            preferences.contentMode = ContentMode.STOIC
        }
        previewContent = WidgetContentSelector.random(this)

        configureModeControls()
        configureMessageControls()
        configureAppearanceControls()

        settingUpControls = false
        renderPreview()
        renderMessages()

        findViewById<Button>(R.id.refreshButton).setOnClickListener {
            StoicWidgetProvider.updateAllWidgets(this)
            previewContent = WidgetContentSelector.random(this)
            renderPreview()
            Toast.makeText(this, R.string.widget_refreshed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureModeControls() {
        personalModeSwitch.isChecked = preferences.contentMode == ContentMode.PERSONAL
        personalModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (settingUpControls) return@setOnCheckedChangeListener

            if (isChecked && messageRepository.count() == 0) {
                settingUpControls = true
                personalModeSwitch.isChecked = false
                settingUpControls = false
                Toast.makeText(this, R.string.add_message_before_enabling, Toast.LENGTH_LONG).show()
                return@setOnCheckedChangeListener
            }

            preferences.contentMode = if (isChecked) {
                ContentMode.PERSONAL
            } else {
                ContentMode.STOIC
            }
            refreshContent()
        }
    }

    private fun configureMessageControls() {
        messageInput.filters = arrayOf(InputFilter.LengthFilter(MessageText.MAX_LENGTH))
        sourceInput.filters = arrayOf(InputFilter.LengthFilter(MessageText.SOURCE_MAX_LENGTH))
        findViewById<Button>(R.id.addMessageButton).setOnClickListener {
            if (!colorsAreValid(lightColorInput, darkColorInput)) return@setOnClickListener
            val message = messageRepository.add(
                text = messageInput.text.toString(),
                source = sourceInput.text.toString(),
                lightTextColor = lightColorInput.text.toString(),
                darkTextColor = darkColorInput.text.toString(),
            )
            if (message == null) {
                Toast.makeText(this, R.string.message_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            messageInput.text.clear()
            sourceInput.text.clear()
            lightColorInput.text.clear()
            darkColorInput.text.clear()
            renderMessages()
            if (preferences.contentMode == ContentMode.PERSONAL) refreshContent()
            Toast.makeText(this, R.string.message_added, Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureAppearanceControls() {
        val themeSpinner = findViewById<Spinner>(R.id.themeSpinner)
        val fontSizeSpinner = findViewById<Spinner>(R.id.fontSizeSpinner)
        val transparentSwitch = findViewById<Switch>(R.id.transparentBackgroundSwitch)

        themeSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.widget_theme_options,
            android.R.layout.simple_spinner_item,
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        themeSpinner.setSelection(preferences.theme.ordinal)
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                preferences.theme = WidgetPreferences.Theme.values()[position]
                appearanceChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        fontSizeSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.widget_font_size_options,
            android.R.layout.simple_spinner_item,
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        fontSizeSpinner.setSelection(preferences.fontSize.ordinal)
        fontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                preferences.fontSize = WidgetPreferences.FontSize.values()[position]
                appearanceChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        transparentSwitch.isChecked = preferences.transparentBackground
        transparentSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.transparentBackground = isChecked
            appearanceChanged()
        }

        attributionSwitch.isChecked = preferences.showAttribution
        attributionSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.showAttribution = isChecked
            appearanceChanged()
        }
    }

    private fun renderPreview() {
        val appearance = WidgetAppearance.resolve(this, previewContent)
        val isPersonalMode = preferences.contentMode == ContentMode.PERSONAL
        quoteText.text = WidgetContentFormatter.quote(previewContent)
        quoteAuthor.text = WidgetContentFormatter.attribution(previewContent).orEmpty()
        contentCount.text = if (isPersonalMode) {
            resources.getQuantityString(
                R.plurals.personal_message_count,
                messageRepository.count(),
                messageRepository.count(),
            )
        } else {
            getString(R.string.quote_count, QuoteRepository.size())
        }
        modeStatus.text = getString(
            if (isPersonalMode) R.string.mode_personal else R.string.mode_stoic,
        )
        previewCard.setBackgroundResource(appearance.backgroundResource)
        quoteText.setTextColor(appearance.quoteColor)
        quoteAuthor.setTextColor(appearance.authorColor)
        quoteText.textSize = 20f * appearance.fontScale
        quoteAuthor.textSize = 14f * appearance.fontScale
        quoteAuthor.visibility = if (
            WidgetContentFormatter.attribution(previewContent) != null &&
                appearance.showAttribution
        ) {
            View.VISIBLE
        } else {
            View.GONE
        }
        attributionSwitch.isEnabled = true
    }

    private fun renderMessages() {
        val messages = messageRepository.all()
        personalMessageCount.text = resources.getQuantityString(
            R.plurals.personal_message_count,
            messages.size,
            messages.size,
        )
        messagesContainer.removeAllViews()

        if (messages.isEmpty()) {
            TextView(this).apply {
                setText(R.string.no_personal_messages)
                setPadding(0, 12, 0, 12)
                messagesContainer.addView(this)
            }
            return
        }

        messages.asReversed().forEach { message ->
            val row = LayoutInflater.from(this)
                .inflate(R.layout.item_personal_message, messagesContainer, false)
            row.findViewById<TextView>(R.id.messageText).text =
                MessageText.titleCaseForDisplay(message.text)
            row.findViewById<TextView>(R.id.messageSource).apply {
                text = MessageText.attributionForDisplay(message.source).orEmpty()
                visibility = if (message.source == null) View.GONE else View.VISIBLE
            }
            row.findViewById<Button>(R.id.editMessageButton).setOnClickListener {
                showEditDialog(message)
            }
            row.findViewById<Button>(R.id.deleteMessageButton).setOnClickListener {
                confirmDelete(message)
            }
            messagesContainer.addView(row)
        }
    }

    private fun showEditDialog(message: PersonalMessage) {
        val container = LayoutInflater.from(this).inflate(R.layout.dialog_personal_message, null)
        val input = container.findViewById<EditText>(R.id.dialogMessageInput).apply {
            setText(message.text)
            setSelection(text.length)
            filters = arrayOf(InputFilter.LengthFilter(MessageText.MAX_LENGTH))
        }
        val source = container.findViewById<EditText>(R.id.dialogSourceInput).apply {
            setText(message.source.orEmpty())
            filters = arrayOf(InputFilter.LengthFilter(MessageText.SOURCE_MAX_LENGTH))
        }
        val lightColor = container.findViewById<EditText>(R.id.dialogLightColorInput).apply {
            setText(message.lightTextColor.orEmpty())
        }
        val darkColor = container.findViewById<EditText>(R.id.dialogDarkColorInput).apply {
            setText(message.darkTextColor.orEmpty())
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.edit_message)
            .setView(container)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.save, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (!colorsAreValid(lightColor, darkColor)) return@setOnClickListener
                if (messageRepository.update(
                        id = message.id,
                        text = input.text.toString(),
                        source = source.text.toString(),
                        lightTextColor = lightColor.text.toString(),
                        darkTextColor = darkColor.text.toString(),
                    )
                ) {
                    dialog.dismiss()
                    renderMessages()
                    if (preferences.contentMode == ContentMode.PERSONAL) refreshContent()
                } else {
                    Toast.makeText(this, R.string.message_required, Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun colorsAreValid(lightColor: EditText, darkColor: EditText): Boolean {
        if (
            MessageText.isValidOptionalColor(lightColor.text.toString()) &&
            MessageText.isValidOptionalColor(darkColor.text.toString())
        ) {
            return true
        }
        Toast.makeText(this, R.string.invalid_text_color, Toast.LENGTH_LONG).show()
        return false
    }

    private fun confirmDelete(message: PersonalMessage) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_message)
            .setMessage(R.string.delete_message_confirmation)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                if (!messageRepository.delete(message.id)) return@setPositiveButton

                if (
                    messageRepository.count() == 0 &&
                    preferences.contentMode == ContentMode.PERSONAL
                ) {
                    preferences.contentMode = ContentMode.STOIC
                    settingUpControls = true
                    personalModeSwitch.isChecked = false
                    settingUpControls = false
                    Toast.makeText(this, R.string.returned_to_stoic_mode, Toast.LENGTH_LONG).show()
                }
                renderMessages()
                refreshContent()
            }
            .show()
    }

    private fun refreshContent() {
        StoicWidgetProvider.updateAllWidgets(this, selectNewContent = true)
        previewContent = WidgetContentSelector.random(this)
        renderPreview()
    }

    private fun appearanceChanged() {
        if (!settingUpControls) {
            StoicWidgetProvider.updateAllWidgets(this, selectNewContent = false)
            renderPreview()
        }
    }
}
