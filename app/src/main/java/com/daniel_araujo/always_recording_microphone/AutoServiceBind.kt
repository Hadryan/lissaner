package com.daniel_araujo.always_recording_microphone

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.*

typealias runnable<T> = (T) -> Unit

/**
 * Just to conveniently communicate with a bound service.
 */
class AutoServiceBind<T : Any> {

    val queue = ArrayList<runnable<T>>()

    val serviceClass: kotlin.reflect.KClass<T>

    val activity: AppCompatActivity

    var binder: AutoServiceBinder<T>? = null

    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as AutoServiceBinder<T>
            work()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
        }
    }

    constructor(serviceClass: kotlin.reflect.KClass<T>, activity: AppCompatActivity) {
        this.serviceClass = serviceClass
        this.activity = activity
    }

    fun run(runner: runnable<T>) {
        queue.add(runner)

        if (binder == null) {
            Intent(activity, serviceClass.java).also {
                activity.bindService(it, serviceConnection, AppCompatActivity.BIND_AUTO_CREATE)
            }
        } else {
            work()
        }
    }

    private fun work() {
        while (queue.size > 0) {
            val runner = queue.removeAt(0)

            runner.invoke(binder!!.service)

            if (binder == null) {
                break;
            }
        }
    }
}

class AutoServiceBinder<T>(val service: T) : Binder() {
}