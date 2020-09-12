package com.daniel_araujo.lissaner.files

import java.io.FileDescriptor
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

    /**
     * Retrieves the timestap of a file. Returns null if file does not exist.
     */
    fun timestamp(name: String): Long?

    /**
     * Retrieves the size of a file. Returns null if file does not exist.
     */
    fun size(name: String): Long?

    /**
     * Retrieves duration in milliseconds. Returns null if file does not exist or if duration can't be read.
     */
    fun duration(name: String): Long?

    /**
     * Opens a file. Returns null if file does not exist.
     */
    fun open(name: String): FileDescriptor?

    /**
     * Deletes file. Does nothing if file does not exist.
     */
    fun delete(name: String)
}