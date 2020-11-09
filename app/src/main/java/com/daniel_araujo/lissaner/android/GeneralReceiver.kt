package com.daniel_araujo.lissaner.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class GeneralReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.v("BootCompletedReceiver", "Received action: " + intent.action)

        val application = context.applicationContext as Application

        application.initialize()
    }
}