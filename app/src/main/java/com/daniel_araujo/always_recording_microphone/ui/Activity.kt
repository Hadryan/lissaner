package com.daniel_araujo.always_recording_microphone.ui

import com.daniel_araujo.always_recording_microphone.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Debug
import android.view.View
import android.widget.Button
import com.daniel_araujo.always_recording_microphone.R

open class Activity : AppCompatActivity() {
    fun getOurApplication() : Application {
        return application as Application
    }
}