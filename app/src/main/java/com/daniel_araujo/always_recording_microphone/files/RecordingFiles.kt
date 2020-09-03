package com.daniel_araujo.always_recording_microphone.files

import java.io.OutputStream

interface RecordingFiles {
    /**
     * Lists files by name.
     */
    fun list(): List<String>

    /**
     * Creates file and opens a stream to write contents to it. The caller is responsible for
     * closing the stream.
     */
    fun create(name: String): OutputStream
}