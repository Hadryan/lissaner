package com.daniel_araujo.lissaner.android.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.shawnlin.numberpicker.NumberPicker

class SettingsOptionSelectNumberView : SettingsOptionBaseView {
    var available: List<Int> = emptyList()
        get() = field
        set(value) {
            field = value

            picker.formatter = NumberPicker.Formatter {
                if (it >= field.size) {
                    return@Formatter "NO";
                }

                val value = field[it]

                formatter?.invoke(value) ?: value.toString()
            }

            // This is inclusive.
            picker.maxValue = if (field.size > 0) field.size - 1 else 0
        }

    var value: Int
        get() = available[picker.value]
        set(value) {
            // The picker starts counting from 1.
            picker.value = available.indexOf(value)
        }

    var formatter: ((Int) -> String)? = null

    var onValueChangedListener: ((Any) -> Unit)? = null

    private val wranglerHack = Runnable {
        onValueChangedListener?.invoke(value)
    }

    private lateinit var picker: NumberPicker;

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
    }

    override fun makeControl(): View {
        picker = NumberPicker(context);

        @SuppressLint("WrongConstant")
        picker.orientation = NumberPicker.HORIZONTAL

        picker.minValue = 0
        picker.maxValue = 0

        val handler = Handler(Looper.getMainLooper())

        picker.setOnValueChangedListener { _, _, _ ->
            // The library calls this listener too many times unnecessarily while scrolling the
            // wheel. So we add a delay to keep this in check.
            handler.removeCallbacks(wranglerHack)
            handler.postDelayed(wranglerHack, 200)
        }

        return picker;
    }
}