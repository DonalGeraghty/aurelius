package com.donalgeraghty.stoicwidget

import android.app.Activity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val quoteText = findViewById<TextView>(R.id.previewQuote)
        val quoteAuthor = findViewById<TextView>(R.id.previewAuthor)
        val quoteCount = findViewById<TextView>(R.id.quoteCount)
        val refreshButton = findViewById<Button>(R.id.refreshButton)
        val previewCard = findViewById<LinearLayout>(R.id.previewCard)
        val themeSpinner = findViewById<Spinner>(R.id.themeSpinner)
        val fontSizeSpinner = findViewById<Spinner>(R.id.fontSizeSpinner)
        val transparentSwitch = findViewById<Switch>(R.id.transparentBackgroundSwitch)
        val attributionSwitch = findViewById<Switch>(R.id.showAttributionSwitch)
        val preferences = WidgetPreferences(this)
        var previewQuote = QuoteRepository.randomQuote()
        var settingUpControls = true

        fun renderPreview() {
            val appearance = WidgetAppearance.resolve(this)
            quoteText.text = "“${previewQuote.text}”"
            quoteAuthor.text = previewQuote.author
            quoteCount.text = getString(R.string.quote_count, QuoteRepository.size())
            previewCard.setBackgroundResource(appearance.backgroundResource)
            quoteText.setTextColor(appearance.quoteColor)
            quoteAuthor.setTextColor(appearance.authorColor)
            quoteText.textSize = 20f * appearance.fontScale
            quoteAuthor.textSize = 14f * appearance.fontScale
            quoteAuthor.visibility = if (appearance.showAttribution) View.VISIBLE else View.GONE
        }

        fun appearanceChanged() {
            if (!settingUpControls) {
                StoicWidgetProvider.updateAllWidgets(this, selectNewQuote = false)
                renderPreview()
            }
        }

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

        settingUpControls = false
        renderPreview()

        refreshButton.setOnClickListener {
            StoicWidgetProvider.updateAllWidgets(this)
            previewQuote = QuoteRepository.randomQuote()
            renderPreview()
            Toast.makeText(this, R.string.widget_refreshed, Toast.LENGTH_SHORT).show()
        }
    }
}
