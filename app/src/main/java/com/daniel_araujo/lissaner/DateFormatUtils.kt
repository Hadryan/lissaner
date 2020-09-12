package com.daniel_araujo.lissaner

import java.text.DateFormat

object DateFormatUtils {
    /**
     * Formats a timestamp into a locale aware date and time text.
     */
    fun localeDateAndTime(timestamp: Long): String {
        return DateFormat.getDateInstance().format(timestamp)
    }

    /**
     * Converts number of seconds to mm:ss
     */
    fun mmss(duration: Int): String {
        val minutes = duration / 60
        val seconds = duration % 60

        return String.format("%02d:%02d", minutes, seconds)
    }
}