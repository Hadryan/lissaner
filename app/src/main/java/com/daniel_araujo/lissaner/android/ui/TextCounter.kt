package com.daniel_araujo.lissaner.android.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import java.util.*

class TextCounter : FrameLayout {
    private lateinit var textView: AppCompatTextView

    /**
     * Sets format.
     */
    val format: String = "$[hh]:$[mm]:$[ss]"

    /**
     * Current time being shown on the view.
     */
    var time: Long
        get() = timeCalendar.timeInMillis
        set(time) {
            timeCalendar.timeInMillis = time
            updateTextView()
        }

    /**
     * Current time as a Calendar object.
     */
    private val timeCalendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT")).also {
        it.timeInMillis = 0
    }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun init(attrs: AttributeSet?, defStyle: Int) {
        textView = AppCompatTextView(context)

        addView(textView)

        updateTextView()
    }

    private fun makeText(): String {
        var result = format;

        result = result.replace("$[hh]", timeCalendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0'))
        result = result.replace("$[mm]", timeCalendar.get(Calendar.MINUTE).toString().padStart(2, '0'))
        result = result.replace("$[ss]", timeCalendar.get(Calendar.SECOND).toString().padStart(2, '0'))

        return result
    }

    private fun updateTextView() {
        textView.text = makeText()
    }
}