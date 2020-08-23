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

    /**
     * Listener that gets called when service is bound.
     */
    var onConnectListener: ((T) -> Unit)? = null

    /**
     * Listener that gets called when service is unbound.
     */
    var onDisconnectListener: (() -> Unit)? = null

    private val queue = ArrayList<runnable<T>>()

    private val serviceClass: kotlin.reflect.KClass<T>

    private val activity: AppCompatActivity

    private var binder: AutoServiceBinder<T>? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            binder = service as AutoServiceBinder<T>
            onConnectListener?.invoke(binder!!.service)
            work()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
            onDisconnectListener?.invoke()
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