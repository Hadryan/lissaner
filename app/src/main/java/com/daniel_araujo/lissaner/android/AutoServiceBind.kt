package com.daniel_araujo.lissaner.android

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.reflect.KClass

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

    private val context: Context

    private var binder: AutoServiceBinder<T>? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (binder != null) {
                // Already bound.
                return;
            }

            // Intentionally crashing here.
            @Suppress("UNCHECKED_CAST")
            binder = service as AutoServiceBinder<T>

            onConnectListener?.invoke(binder!!.service)
            work()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            onDisconnect()
        }

        override fun onBindingDied(name: ComponentName?) {
            onDisconnect()
        }
    }

    constructor(serviceClass: KClass<T>, context: Context) {
        this.serviceClass = serviceClass
        this.context = context
    }

    fun bind() {
        if (binder == null) {
            Intent(context, serviceClass.java).also {
                context.bindService(it, serviceConnection, AppCompatActivity.BIND_AUTO_CREATE)
            }
        }
    }

    fun unbind() {
        if (binder != null) {
            context.unbindService(serviceConnection)

            // Apparently Android does not call onServiceDisconnected if I unbind manually. The
            // documentation states "typically happens when the process hosting the service has
            // crashed or been killed"
            onDisconnect()
        }
    }

    fun run(runner: runnable<T>) {
        queue.add(runner)

        if (binder == null) {
            // Bind automatically.
            bind()
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

    private fun onDisconnect() {
        binder = null
        queue.clear()
        onDisconnectListener?.invoke()
    }
}

class AutoServiceBinder<T>(val service: T) : Binder() {
}