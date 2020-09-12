package com.daniel_araujo.lissaner

import java.io.OutputStream

class NullOutputStream : OutputStream() {
    override fun write(p0: Int) {
        // Do nothing.
    }
}