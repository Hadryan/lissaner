package com.daniel_araujo.always_recording_microphone.android

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import com.daniel_araujo.always_recording_microphone.files.RecordingFiles
import java.io.File
import java.io.OutputStream
import java.lang.Exception

class AndroidRecordingFiles : RecordingFiles {
    /**
     * For retrieving duration.
     */
    private val mmr = MediaMetadataRetriever()

    /**
     * Directory where recordings are placed.
     */
    private val recordingsDir: File

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

    override fun timestamp(name: String): Long? {
        val file = File(recordingsDir, name)

        if (file.exists()) {
            return file.lastModified()
        } else {
            return null
        }
    }

    override fun size(name: String): Long? {
        val file = File(recordingsDir, name)

        if (file.exists()) {
            return file.length()
        } else {
            return null
        }
    }

    override fun duration(name: String): Int? {
        val file = File(recordingsDir, name)

        try {
            file.outputStream().use {
                mmr.setDataSource(it.fd)
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Failed to set data source.", e)
            return null
        }

        val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)

        if (durationStr == null) {
            Log.e(javaClass.simpleName, "METADATA_KEY_DURATION returned null")
            return null
        }

        return Integer.parseInt(durationStr);
    }
}