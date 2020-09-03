package com.daniel_araujo.always_recording_microphone.android.ui

/**
 * Base fragment of all fragments in this project.
 */
open class Fragment : androidx.fragment.app.Fragment() {
    val ourActivity: Activity
        get() = activity as Activity
}