package com.daniel_araujo.lissaner

import java.io.InputStream
import java.util.*

object IOUtils {
    fun readAll(stream: InputStream?, charset: String = "UTF-8"): String {
        val s = Scanner(stream, charset).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}