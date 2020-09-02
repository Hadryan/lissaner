package com.daniel_araujo.always_recording_microphone.android.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.daniel_araujo.always_recording_microphone.R

/**
 * TODO: document your custom view class.
 */
class WellView : FrameLayout {

    private lateinit var bgPaint: Paint

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

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        setWillNotDraw(false)
        // Set up a default TextPaint object
        bgPaint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = ContextCompat.getColor(context, R.color.well)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val rect = Rect()
        getDrawingRect(rect)
        canvas.drawRect(rect, bgPaint)
        super.onDraw(canvas)
    }
}
