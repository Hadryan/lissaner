package com.daniel_araujo.always_recording_microphone.android

import android.content.Context
import com.daniel_araujo.always_recording_microphone.files.RecordingFiles
import java.io.File
import java.io.OutputStream

class AndroidRecordingFiles : RecordingFiles {
    val recordingsDir: File

    constructor(context: Context) {
        recordingsDir = File(context.getExternalFilesDir(null), "Recordings")

        if (!recordingsDir.exists()) {
            // Gotta create it.
            if (!recordingsDir.mkdirs()) {
                throw RuntimeException("Failed to create recordings directory.");
            }
        }
    }

    override fun list(): List<String> {
        return recordingsDir.listFiles().map { it.name }
    }

    override fun create(name: String): OutputStream {
        val file = File(recordingsDir, name)

        return file.outputStream()
    }
}