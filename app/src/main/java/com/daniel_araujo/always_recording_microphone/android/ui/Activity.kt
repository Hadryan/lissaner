package com.daniel_araujo.always_recording_microphone.android.ui

import com.daniel_araujo.always_recording_microphone.android.Application
import androidx.appcompat.app.AppCompatActivity

open class Activity : AppCompatActivity() {
    val ourApplication: Application
        get() = application as Application
}