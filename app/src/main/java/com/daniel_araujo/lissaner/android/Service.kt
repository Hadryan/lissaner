package com.daniel_araujo.lissaner.android

open abstract class Service : android.app.Service() {
    val ourApplication: Application
        get() = application as Application
}