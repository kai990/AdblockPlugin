package com.spaceship.adblock.plugin

import android.app.Application
import com.spaceship.netblocker.Env
import com.spaceship.netblocker.NetBlocker

class PluginApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Env.init(this)
        NetBlocker.init(this,"com.spaceship.netprotect")
    }
}