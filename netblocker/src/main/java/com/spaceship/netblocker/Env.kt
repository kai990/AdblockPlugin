package com.spaceship.netblocker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.spaceship.netblocker.utils.DeviceStorageApp

@SuppressLint("StaticFieldLeak")
object Env {
    private lateinit var originContext: Context

    private lateinit var context: Context

    private lateinit var storageContext: Context
    fun init(ctx: Context) {
        originContext = ctx
        context = originContext
        storageContext = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            originContext
        } else {
            DeviceStorageApp(ctx)
        }
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
