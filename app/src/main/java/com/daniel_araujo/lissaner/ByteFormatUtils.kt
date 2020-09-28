package com.daniel_araujo.lissaner

import android.content.Context

/**
 * Methods for formatting byte sizes and stuff.
 */
object ByteFormatUtils {
    /**
     * Returns size with as few digits as possible.
     */
    fun shortSize(context: Context, size: Long): String {
        // TODO: Replace android lib if this ever gets called by code that is unit tested outside
        //  Android environment.
        return android.text.format.Formatter.formatShortFileSize(context, size)
    }

    /**
     * Returns size with as few digits as possible.
     */
    fun shortSize(context: Context, size: Int): String {
        return shortSize(context, size.toLong())
    }
}