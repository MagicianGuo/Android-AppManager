package com.gjc.appmanager

import android.app.Application

class App : Application() {
    companion object {
        private lateinit var sApp: App
        fun get() = sApp
    }

    override fun onCreate() {
        super.onCreate()
        sApp = this
    }
}