package com.donalgeraghty.stoicwidget

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
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
    private lateinit var personalMessageCount: TextView
    private lateinit var collectionSpinner: Spinner
    private lateinit var lightColorSwatch: View
    private lateinit var darkColorSwatch: View
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
        personalMessageCount = findViewById(R.id.personalMessageCount)
        collectionSpinner = findViewById(R.id.collectionSpinner)
        lightColorSwatch = findViewById(R.id.lightColorSwatch)
        darkColorSwatch = findViewById(R.id.darkColorSwatch)
        attributionSwitch = findViewById(R.id.showAttributionSwitch)
        preferences = WidgetPreferences(this)
        messageRepository = PersonalMessageRepository(this)
        ensureValidPersonalMode()
        previewContent = WidgetContentSelector.random(this)
        configureModeControls()
        configureCardControls()
        configureAppearanceControls()
        findViewById<Button>(R.id.refreshButton).setOnClickListener {
            StoicWidgetProvider.updateAllWidgets(this)
            previewContent = WidgetContentSelector.random(this)
            renderPreview()
            Toast.makeText(this, R.string.widget_refreshed, Toast.LENGTH_SHORT).show()
        }
        settingUpControls = false
        renderCardSummary()
        renderPreview()
    }

    override fun onResume() {
        super.onResume()
        if (!::messageRepository.isInitialized) return
        ensureValidPersonalMode()
        configureCollectionSpinner()
        renderCardSummary()
        previewContent = WidgetContentSelector.random(this)
        renderPreview()
    }

    private fun ensureValidPersonalMode() {
        val selectedCollection = preferences.selectedCollection
        if (
            selectedCollection != null &&
            messageRepository.enabledCount(selectedCollection) == 0
        ) {
            preferences.selectedCollection = null
        }
        if (preferences.contentMode == ContentMode.PERSONAL && messageRepository.enabledCount() == 0) {
            preferences.contentMode = ContentMode.STOIC
            if (::personalModeSwitch.isInitialized) {
                settingUpControls = true
                personalModeSwitch.isChecked = false
                settingUpControls = false
            }
        }
    }

    private fun configureModeControls() {
        personalModeSwitch.isChecked = preferences.contentMode == ContentMode.PERSONAL
        personalModeSwitch.setOnCheckedChangeListener { _, checked ->
            if (settingUpControls) return@setOnCheckedChangeListener
            if (checked && messageRepository.enabledCount() == 0) {
                settingUpControls = true
                personalModeSwitch.isChecked = false
                settingUpControls = false
                Toast.makeText(this, R.string.add_message_before_enabling, Toast.LENGTH_LONG).show()
                return@setOnCheckedChangeListener
            }
            preferences.contentMode = if (checked) ContentMode.PERSONAL else ContentMode.STOIC
            refreshContent()
        }
    }

    private fun configureCardControls() {
        findViewById<Button>(R.id.manageCardsButton).setOnClickListener {
            startActivity(Intent(this, CustomCardsActivity::class.java))
        }
        configureCollectionSpinner()
    }

    private fun configureCollectionSpinner() {
        val options = listOf(getString(R.string.all_collections)) + messageRepository.collections()
        val selected = preferences.selectedCollection
        val selectedPosition = selected?.let(options::indexOf)?.takeIf { it >= 0 } ?: 0
        if (selectedPosition == 0 && selected != null) preferences.selectedCollection = null
        collectionSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            options,
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        collectionSpinner.setSelection(selectedPosition)
        collectionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            private var ignoreInitialSelection = true

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (ignoreInitialSelection) {
                    ignoreInitialSelection = false
                    return
                }
                preferences.selectedCollection = options.getOrNull(position)?.takeIf { position > 0 }
                if (preferences.contentMode == ContentMode.PERSONAL) refreshContent()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun configureAppearanceControls() {
        val themeSpinner = findViewById<Spinner>(R.id.themeSpinner)
        val fontSizeSpinner = findViewById<Spinner>(R.id.fontSizeSpinner)
        val transparentSwitch = findViewById<Switch>(R.id.transparentBackgroundSwitch)
        findViewById<View>(R.id.lightColorControl).setOnClickListener {
            ColorPickerDialog.show(this, getString(R.string.choose_light_text_color), currentLightTextColor()) { color ->
                preferences.lightTextColor = color
                updateColorSwatches()
                appearanceChanged()
            }
        }
        findViewById<View>(R.id.darkColorControl).setOnClickListener {
            ColorPickerDialog.show(this, getString(R.string.choose_dark_text_color), currentDarkTextColor()) { color ->
                preferences.darkTextColor = color
                updateColorSwatches()
                appearanceChanged()
            }
        }
        findViewById<Button>(R.id.resetTextColorsButton).setOnClickListener {
            preferences.resetTextColors()
            updateColorSwatches()
            appearanceChanged()
        }
        updateColorSwatches()
        themeSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.widget_theme_options,
            android.R.layout.simple_spinner_item,
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        themeSpinner.setSelection(preferences.theme.ordinal)
        themeSpinner.onItemSelectedListener = enumSpinnerListener { position ->
            preferences.theme = WidgetPreferences.Theme.values()[position]
            appearanceChanged()
        }
        fontSizeSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.widget_font_size_options,
            android.R.layout.simple_spinner_item,
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        fontSizeSpinner.setSelection(preferences.fontSize.ordinal)
        fontSizeSpinner.onItemSelectedListener = enumSpinnerListener { position ->
            preferences.fontSize = WidgetPreferences.FontSize.values()[position]
            appearanceChanged()
        }
        transparentSwitch.isChecked = preferences.transparentBackground
        transparentSwitch.setOnCheckedChangeListener { _, checked ->
            preferences.transparentBackground = checked
            appearanceChanged()
        }
        attributionSwitch.isChecked = preferences.showAttribution
        attributionSwitch.setOnCheckedChangeListener { _, checked ->
            preferences.showAttribution = checked
            appearanceChanged()
        }
    }

    private fun enumSpinnerListener(action: (Int) -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = action(position)
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }

    private fun renderCardSummary() {
        val count = messageRepository.count()
        personalMessageCount.text = resources.getQuantityString(R.plurals.personal_message_count, count, count)
    }

    private fun renderPreview() {
        val appearance = WidgetAppearance.resolve(this)
        val isPersonal = preferences.contentMode == ContentMode.PERSONAL
        quoteText.text = WidgetContentFormatter.quote(previewContent)
        quoteAuthor.text = WidgetContentFormatter.attribution(previewContent).orEmpty()
        contentCount.text = if (isPersonal) {
            val count = messageRepository.enabledCount(preferences.selectedCollection)
            resources.getQuantityString(R.plurals.active_card_count, count, count)
        } else {
            getString(R.string.quote_count, QuoteRepository.size())
        }
        modeStatus.text = getString(if (isPersonal) R.string.mode_personal else R.string.mode_stoic)
        previewCard.setBackgroundResource(appearance.backgroundResource)
        quoteText.setTextColor(appearance.quoteColor)
        quoteAuthor.setTextColor(appearance.authorColor)
        quoteText.textSize = 20f * appearance.fontScale
        quoteAuthor.textSize = 14f * appearance.fontScale
        quoteAuthor.visibility = if (
            WidgetContentFormatter.attribution(previewContent) != null && appearance.showAttribution
        ) View.VISIBLE else View.GONE
    }

    private fun updateColorSwatches() {
        setSwatchColor(lightColorSwatch, currentLightTextColor())
        setSwatchColor(darkColorSwatch, currentDarkTextColor())
    }

    private fun currentLightTextColor(): Int = preferences.lightTextColor ?: getColor(R.color.widget_text_light)
    private fun currentDarkTextColor(): Int = preferences.darkTextColor ?: getColor(R.color.widget_text_dark)

    private fun setSwatchColor(view: View, color: Int) {
        val density = resources.displayMetrics.density
        view.background = GradientDrawable().apply {
            cornerRadius = 6f * density
            setColor(color)
            setStroke(density.toInt(), Color.GRAY)
        }
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
