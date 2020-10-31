package com.daniel_araujo.lissaner.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.use
import com.daniel_araujo.lissaner.R

abstract class SettingsOptionBaseView : FrameLayout {
    /**
     * Name of the setting.
     */
    var name: String = ""
        get() = field
        set(value) {
            val view = findViewById<TextView>(R.id.name)
            view.text = value

            field = value
        }

    /**
     * Short description of the setting.
     */
    var description: String = ""
        get() = field
        set(value) {
            val view = findViewById<TextView>(R.id.description)

            if (!value.isEmpty()) {
                view.text = value
                view.visibility = View.VISIBLE
            } else {
                view.visibility = View.GONE
            }

            field = value
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
        inflate(context, R.layout.view_settings_option_base, this)

        // Load attributes
        context.obtainStyledAttributes(attrs, R.styleable.SettingsOptionBaseView, defStyle, 0).use {
            val nameAttr = it.getString(R.styleable.SettingsOptionBaseView_name)

            if (nameAttr != null) {
                name = nameAttr
            } else {
                name = name
            }

            val descriptionAttr = it.getString(R.styleable.SettingsOptionBaseView_description)

            if (descriptionAttr != null) {
                description = descriptionAttr
            } else {
                description = description
            }
        }

        val wrapper = findViewById<FrameLayout>(R.id.control_wrapper)
        wrapper.addView(makeControl())
    }

    protected abstract fun makeControl(): View
}