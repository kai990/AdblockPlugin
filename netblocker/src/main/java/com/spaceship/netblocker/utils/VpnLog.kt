package com.spaceship.netblocker.utils

import com.spaceship.netblocker.utils.Slog

/**
 * @author John
 * @since 2019-05-19 17:19
 */

fun writeLog(format: String, vararg args: Any) {
    val logString = String.format(format, *args)
    Slog.d("WriteLog", logString)
}