package com.daniel_araujo.always_recording_microphone

import java.io.OutputStream

class NullOutputStream : OutputStream() {
    override fun write(p0: Int) {
        // Do nothing.
    }
}