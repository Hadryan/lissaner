package com.daniel_araujo.lissaner

import java.io.OutputStream

class NullOutputStream : OutputStream {
    val delay: Long

    constructor(delay: Long = 0) {
        this.delay = delay
    }

    override fun write(p0: Int) {
        if (delay > 0) {
            Thread.sleep(delay)
        }
    }
}