package com.daniel_araujo.always_recording_microphone.android.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.daniel_araujo.always_recording_microphone.R

class AudioPlayerView : FrameLayout {
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
        inflate(context, R.layout.view_audio_player, this)
    }
}
