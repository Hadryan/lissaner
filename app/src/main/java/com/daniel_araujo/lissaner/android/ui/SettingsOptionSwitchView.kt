package com.daniel_araujo.lissaner.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Switch

class SettingsOptionSwitchView : SettingsOptionBaseView {
    private lateinit var switch: Switch;

    var value: Boolean
        get() = switch.isChecked
        set(value) {
            switch.isChecked = value
        }

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
        switch = Switch(context);
        return switch;
    }
}