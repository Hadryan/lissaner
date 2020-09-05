package com.daniel_araujo.always_recording_microphone.android

import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewConfiguration

class LongClickRepeatTouchListener : OnTouchListener {
    private var handler: Handler? = null

    private var view: View? = null

    private var repeatDelay: Long = 0

    private val handlerRunnable: Runnable = object : Runnable {
        override fun run() {
            handler!!.postDelayed(this, repeatDelay)
            view!!.performLongClick()
        }
    }

    constructor(delay: Long = 100) {
        this.repeatDelay = delay
    }

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                handler = view.handler
                handler!!.removeCallbacks(handlerRunnable)
                handler!!.postDelayed(handlerRunnable, ViewConfiguration.getLongPressTimeout().toLong())
                this.view = view
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler!!.removeCallbacks(handlerRunnable)
                handler = null
                this.view = null
            }
        }

        // So that Android performs default transitions.
        return false
    }
}