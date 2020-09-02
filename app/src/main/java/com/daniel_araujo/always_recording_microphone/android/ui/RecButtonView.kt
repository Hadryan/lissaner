package com.daniel_araujo.always_recording_microphone.android.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.daniel_araujo.always_recording_microphone.R


/**
 * TODO: document your custom view class.
 */
class RecButtonView : View {
    /**
     * Values that are cached so as to not slow down the onDraw method.
     */
    private val cache = object {
        var centerX = 0f
        var centerY = 0f
        var radius = 0f
        var radiusBorder = 0f
        var textStartCenter = 0f
        var textStopCenter = 0f
    }

    /**
     * Objects used for drawing.
     */
    private val drawTools = object {
        lateinit var borderPaint: Paint

        lateinit var bgPaint: Paint

        lateinit var activatePaint: Paint

        lateinit var textPaint: Paint
    }

    /**
     * Text for start.
     */
    val textStart = "START"

    /**
     * Text for stop
     */
    val textStop = "STOP"

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
        val bgColor = ContextCompat.getColor(context, R.color.colorRecord)

        drawTools.bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            setStyle(Paint.Style.FILL);
            setColor(bgColor);
        }

        drawTools.activatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            setColor(ColorUtils.blendARGB(bgColor, Color.BLACK, 0.4f))
            setStyle(Paint.Style.FILL);
        }

        drawTools.borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            setColor(ColorUtils.blendARGB(bgColor, Color.BLACK, 0.2f))
            setStyle(Paint.Style.STROKE)
            strokeWidth = 20f
        }

        drawTools.textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            val isBgBright = ColorUtils.calculateLuminance(bgColor) > 0.5
            val textColor = if (isBgBright) Color.BLACK else Color.WHITE
            color = textColor
            textAlign = Paint.Align.CENTER
        }
    }

    override fun onDraw(canvas: Canvas) {
        // The inside.
        if (isActivated) {
            canvas.drawCircle(cache.centerX, cache.centerY, cache.radius, drawTools.activatePaint)
        } else {
            canvas.drawCircle(cache.centerX, cache.centerY, cache.radius, drawTools.bgPaint)
        }

        // The perimeter.
        canvas.drawCircle(cache.centerX, cache.centerY, cache.radiusBorder, drawTools.borderPaint)

        // The text.
        if (isActivated) {
            canvas.drawText(textStop, cache.centerX, cache.centerY - cache.textStopCenter, drawTools.textPaint)
        } else {
            canvas.drawText(textStart, cache.centerX, cache.centerY - cache.textStartCenter, drawTools.textPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        cache.centerX = w.toFloat() / 2;
        cache.centerY = h.toFloat() / 2;
        cache.radius = w.toFloat() / 2;
        cache.radiusBorder - drawTools.borderPaint.strokeWidth / 2

        drawTools.textPaint.textSize = (cache.centerY * 0.6).toFloat()

        val textBounds = Rect()

        drawTools.textPaint.getTextBounds(textStart, 0, textStart.length, textBounds);
        cache.textStartCenter = textBounds.exactCenterY()

        drawTools.textPaint.getTextBounds(textStop, 0, textStop.length, textBounds);
        cache.textStopCenter = textBounds.exactCenterY()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        // First of, respect minimum size.
        if (width < suggestedMinimumWidth) {
            width = suggestedMinimumWidth
        }
        if (height < suggestedMinimumHeight) {
            height = suggestedMinimumHeight
        }

        // The button must be round. Pick the smallest side and assign it to the other side.
        if (width > height) {
            width = height
        } else {
            height = width
        }

        setMeasuredDimension(width, height)
    }
}