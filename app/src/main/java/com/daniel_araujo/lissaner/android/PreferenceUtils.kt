package com.daniel_araujo.lissaner.android

import android.content.SharedPreferences

object PreferenceUtils {
    /**
     * Checks that a key exists and that its value is an int.
     */
    fun hasInt(preferences: SharedPreferences, key: String): Boolean {
        if (!preferences.contains(key)) {
            return false
        }

        try {
            // Making sure the type is right.
            preferences.getInt(key, 0)
        } catch (e: Exception) {
            return false
        }

        return true
    }

    /**
     * Checks that a key exists and that its value is a long.
     */
    fun hasLong(preferences: SharedPreferences, key: String): Boolean {
        if (!preferences.contains(key)) {
            return false
        }

        try {
            // Making sure the type is right.
            preferences.getLong(key, 0)
        } catch (e: Exception) {
            return false
        }

        return true
    }

    /**
     * Checks that a key exists and that its value is a long.
     */
    fun hasBoolean(preferences: SharedPreferences, key: String): Boolean {
        if (!preferences.contains(key)) {
            return false
        }

        try {
            // Making sure the type is right.
            preferences.getBoolean(key, false)
        } catch (e: Exception) {
            return false
        }

        return true
    }

    /**
     * Retrieves an int value. Throws an exception if the value does not exist or is of the wrong
     * type.
     */
    fun getIntOrFail(preferences: SharedPreferences, key: String): Int {
        if (!preferences.contains(key)) {
            throw java.lang.Exception("Key ${key} does not have a value.")
        }

        return preferences.getInt(key, 0)
    }

    /**
     * Retrieves a long value. Throws an exception if the value does not exist or is of the wrong
     * type.
     */
    fun getLongOrFail(preferences: SharedPreferences, key: String): Long {
        if (!preferences.contains(key)) {
            throw java.lang.Exception("Key ${key} does not have a value.")
        }

        return preferences.getLong(key, 0)
    }

    /**
     * Retrieves a long value. Throws an exception if the value does not exist or is of the wrong
     * type.
     */
    fun getBooleanOrFail(preferences: SharedPreferences, key: String): Boolean {
        if (!preferences.contains(key)) {
            throw java.lang.Exception("Key ${key} does not have a value.")
        }

        return preferences.getBoolean(key, false)
    }
}