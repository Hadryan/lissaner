package com.daniel_araujo.lissaner.android

import android.widget.Spinner
import android.widget.SpinnerAdapter

object SpinnerUtils {
    fun<T> selectItemByValue(spinner: Spinner, value: T) {
        val adapter = spinner.adapter as SpinnerAdapter

        for (position in 0 until adapter.getCount()) {
            if (adapter.getItem(position).equals(value)) {
                // Got it.
                spinner.setSelection(position)
                return
            }
        }
    }
}