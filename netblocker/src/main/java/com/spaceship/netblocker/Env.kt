package com.spaceship.netblocker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.spaceship.netblocker.utils.DeviceStorageApp
import com.spaceship.netblocker.utils.logd

@SuppressLint("StaticFieldLeak")
object Env {
    private lateinit var originContext: Context

    private lateinit var context: Context

    private lateinit var storageContext: Context
    fun init(ctx: Context) {
        logd("xxx","1")
        originContext = ctx
        context = originContext
        storageContext = originContext
        logd("xxx","2")
    }

    @JvmStatic
    fun getApp(): Context {
        return context
    }

    fun storageContext(): Context {
        return storageContext
    }

    fun updateContextConfig(config: Configuration) {
        context = originContext.createConfigurationContext(config)
    }
}
