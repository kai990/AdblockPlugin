package com.spaceship.netblocker.utils

import android.widget.Toast
import androidx.annotation.StringRes
import com.spaceship.netblocker.BuildConfig
import com.spaceship.netblocker.Env
import com.spaceship.netblocker.utils.extensions.safeRun
import com.spaceship.netblocker.utils.thread.ThreadPool.ui



fun toast(str: String?, isLong: Boolean = false, forTest: Boolean = false) {
    if (str.isNullOrEmpty()) return
    if (forTest && !BuildConfig.DEBUG) return
    ui {
        safeRun { Toast.makeText(Env.getApp(), str, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show() }
    }
}

fun toast(@StringRes strRes: Int, isLong: Boolean = false) {
    toast(Env.getApp().getString(strRes), isLong)
}
