package com.spaceship.netblocker.utils

import com.spaceship.netblocker.utils.Slog



fun writeLog(format: String, vararg args: Any) {
    val logString = String.format(format, *args)
    Slog.d("WriteLog", logString)
}