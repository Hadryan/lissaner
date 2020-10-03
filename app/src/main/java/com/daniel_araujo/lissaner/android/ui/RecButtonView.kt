package com.daniel_araujo.lissaner.android.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.FrameLayout
import com.daniel_araujo.lissaner.R

class RecButtonView : FrameLayout {
    private lateinit var buttonView: Button

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
        buttonView = Button(context)
        buttonView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        buttonView.setBackgroundResource(R.drawable.view_rec_button_background);

        buttonView.setOnClickListener {
            isActivated = !isActivated
            callOnClick()
        }

        addView(buttonView)
    }

    override fun setActivated(activated: Boolean) {
        super.setActivated(activated)
        buttonView.isActivated = activated
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

        // Gotta call the super method for the FrameLayout to work. Otherwise child views are not
        // rendered.
        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
    }
}