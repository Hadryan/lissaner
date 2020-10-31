package com.daniel_araujo.lissaner.android.ui

import android.R
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.daniel_araujo.lissaner.android.SpinnerUtils

class SettingsOptionSelectValueView : SettingsOptionBaseView {
    private lateinit var spinner: Spinner;

    var available: List<Int> = emptyList()
        get() = field
        set(value) {
            spinner.adapter = ArrayAdapter(context, R.layout.simple_spinner_item, value)
            field = value
        }

    var value: Any
        get() = spinner.selectedItem
        set(value) {
            SpinnerUtils.selectItemByValue(spinner, value)
        }

    var onValueChangedListener: ((Any) -> Unit)? = null

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
        spinner = Spinner(context);

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing.
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                onValueChangedListener?.invoke(value)
            }
        }

        return spinner;
    }
}