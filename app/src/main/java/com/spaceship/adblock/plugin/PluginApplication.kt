package com.spaceship.adblock.plugin

import android.app.Application
import com.spaceship.netblocker.Env

class PluginApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Env.init(this)
    }
}