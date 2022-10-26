

package com.spaceship.netblocker.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Application
import android.content.Context

@SuppressLint("Registered")
@TargetApi(24)
class DeviceStorageApp(context: Context) : Application() {
    init {
        attachBaseContext(context.createDeviceProtectedStorageContext())
    }

    
    override fun getApplicationContext() = this
}
