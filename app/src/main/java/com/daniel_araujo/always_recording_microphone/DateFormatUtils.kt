package com.daniel_araujo.always_recording_microphone

import java.text.DateFormat

object DateFormatUtils {
    /**
     * Formats a timestamp into a locale aware date and time text.
     */
    fun localeDateAndTime(timestamp: Long): String {
        return DateFormat.getDateInstance().format(timestamp)
    }
}