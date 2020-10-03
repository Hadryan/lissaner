package com.daniel_araujo.lissaner.android

abstract class Service : android.app.Service() {
    val ourApplication: Application
        get() = application as Application
}