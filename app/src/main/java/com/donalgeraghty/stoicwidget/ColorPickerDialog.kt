package com.donalgeraghty.stoicwidget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object ColorPickerDialog {
    fun show(
        context: Context,
        title: String,
        initialColor: Int,
        onColorSelected: (Int) -> Unit,
    ) {
        val content = LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null)
        val preview = content.findViewById<View>(R.id.colorPreview)
        val red = content.findViewById<SeekBar>(R.id.redSeekBar)
        val green = content.findViewById<SeekBar>(R.id.greenSeekBar)
        val blue = content.findViewById<SeekBar>(R.id.blueSeekBar)

        red.max = COLOR_COMPONENT_MAX
        green.max = COLOR_COMPONENT_MAX
        blue.max = COLOR_COMPONENT_MAX
        red.progress = Color.red(initialColor)
        green.progress = Color.green(initialColor)
        blue.progress = Color.blue(initialColor)

        fun selectedColor(): Int = Color.rgb(red.progress, green.progress, blue.progress)

        val density = context.resources.displayMetrics.density
        val previewBackground = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 12f * density
            setStroke((1f * density).toInt(), context.getColor(R.color.parchment_outline))
        }
        preview.background = previewBackground

        fun updatePreview() {
            previewBackground.setColor(selectedColor())
        }

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updatePreview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        }
        red.setOnSeekBarChangeListener(listener)
        green.setOnSeekBarChangeListener(listener)
        blue.setOnSeekBarChangeListener(listener)
        updatePreview()

        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(content)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.apply) { _, _ -> onColorSelected(selectedColor()) }
            .show()
    }

    private const val COLOR_COMPONENT_MAX = 255
}
