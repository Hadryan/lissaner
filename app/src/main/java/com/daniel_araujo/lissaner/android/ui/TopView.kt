package com.daniel_araujo.lissaner.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.res.use
import androidx.core.view.marginLeft
import com.daniel_araujo.lissaner.R

class TopView : FrameLayout {
    var title: String? = null
        get() = field
        set(value) {
            field = value

            val textView = findViewById<TextView>(R.id.title)

            if (field != null) {
                textView.setText(field)
                textView.visibility = View.VISIBLE
            } else {
                textView.visibility = View.GONE
            }
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

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        View.inflate(context, R.layout.view_top, this)

        context.obtainStyledAttributes(attrs, R.styleable.TopView, defStyle, 0).use {
            title = it.getString(R.styleable.TopView_title)
        }
    }

    fun setLeftButton(resId: Int, onClickListener: OnClickListener) {
        val button = findViewById<AppCompatImageButton>(R.id.left_button)
        button.setImageResource(resId)
        button.visibility = View.VISIBLE

        // I tried putting a negative margin on the button but it would clip.

        val layout = findViewById<LinearLayout>(R.id.top_layout)
        layout.setPadding(0, 0, 0, 0)

        button.setOnClickListener(onClickListener)
    }
}
