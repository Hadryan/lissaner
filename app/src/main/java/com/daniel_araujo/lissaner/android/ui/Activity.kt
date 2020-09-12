package com.daniel_araujo.lissaner.android.ui

import com.daniel_araujo.lissaner.android.Application
import androidx.appcompat.app.AppCompatActivity

open class Activity : AppCompatActivity() {
    val ourApplication: Application
        get() = application as Application
}