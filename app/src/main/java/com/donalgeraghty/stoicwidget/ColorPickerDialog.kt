package com.donalgeraghty.stoicwidget

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import kotlin.math.roundToInt

object ColorPickerDialog {
    fun show(
        context: Context,
        title: String,
        initialColor: Int,
        onColorSelected: (Int) -> Unit,
    ) {
        val content = LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null)
        val preview = content.findViewById<View>(R.id.colorPreview)
        val hue = content.findViewById<SeekBar>(R.id.hueSeekBar)
        val saturation = content.findViewById<SeekBar>(R.id.saturationSeekBar)
        val brightness = content.findViewById<SeekBar>(R.id.brightnessSeekBar)
        val hsv = FloatArray(3)
        Color.colorToHSV(initialColor, hsv)

        hue.max = HUE_MAX
        saturation.max = PERCENT_MAX
        brightness.max = PERCENT_MAX
        hue.progress = hsv[0].roundToInt()
        saturation.progress = (hsv[1] * PERCENT_MAX).roundToInt()
        brightness.progress = (hsv[2] * PERCENT_MAX).roundToInt()

        fun selectedColor(): Int = Color.HSVToColor(
            floatArrayOf(
                hue.progress.toFloat(),
                saturation.progress / PERCENT_MAX.toFloat(),
                brightness.progress / PERCENT_MAX.toFloat(),
            ),
        )

        fun updatePreview() {
            val density = context.resources.displayMetrics.density
            preview.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8f * density
                setColor(selectedColor())
                setStroke((1f * density).toInt(), Color.GRAY)
            }
        }

        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updatePreview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        }
        hue.setOnSeekBarChangeListener(listener)
        saturation.setOnSeekBarChangeListener(listener)
        brightness.setOnSeekBarChangeListener(listener)
        updatePreview()

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(content)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.apply) { _, _ -> onColorSelected(selectedColor()) }
            .show()
    }

    private const val HUE_MAX = 360
    private const val PERCENT_MAX = 100
}
