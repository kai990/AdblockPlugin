package com.spaceship.netblocker.utils.extensions

import com.spaceship.netblocker.BuildConfig
import com.spaceship.netblocker.utils.Slog




fun safeRun(printLog: Boolean = true, block: () -> Unit) {
    return try {
        block()
    } catch (e: Throwable) {
        if (printLog && BuildConfig.DEBUG) {
            Slog.e(e)
        } else {
        }
    }
}