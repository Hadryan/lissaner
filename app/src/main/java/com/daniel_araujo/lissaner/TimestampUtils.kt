package com.daniel_araujo.lissaner

object TimestampUtils {
    fun milliToMinutes(t: Long): Long {
        return t / (60 * 1000)
    }

    fun minutesToMilli(t: Long): Long {
        return t * (60 * 1000)
    }
}