package com.donalgeraghty.stoicwidget

import android.app.Activity
import android.os.Bundle
import android.widget.Button
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

        fun renderPreview() {
            val quote = QuoteRepository.quoteForCurrentHour()
            quoteText.text = "“${quote.text}”"
            quoteAuthor.text = quote.author
            quoteCount.text = getString(R.string.quote_count, QuoteRepository.size())
        }

        renderPreview()

        refreshButton.setOnClickListener {
            StoicWidgetProvider.updateAllWidgets(this)
            renderPreview()
            Toast.makeText(this, R.string.widget_refreshed, Toast.LENGTH_SHORT).show()
        }
    }
}
