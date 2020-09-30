package com.daniel_araujo.lissaner.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import com.daniel_araujo.lissaner.R

class LicenseEntryView : FrameLayout {
    var name: String? = null
        get() = field
        set(value) {
            field = value

            val name = findViewById<TextView>(R.id.name)
            name.text = field
        }

    var text: String? = null
        get() = field
        set(value) {
            field = value

            val text = findViewById<TextView>(R.id.text)
            text.text = field
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
        View.inflate(context, R.layout.view_license_entry, this)
    }
}
