package com.spaceship.adblock.plugin

import android.app.Application
import android.content.Context
import com.spaceship.netblocker.Env
import com.spaceship.netblocker.NetBlocker

class PluginApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        Env.init(this)
        NetBlocker.init(this, BuildConfig.APPLICATION_ID)
    }
}