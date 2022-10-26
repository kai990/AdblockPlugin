package com.spaceship.netblocker.utils

import com.spaceship.netblocker.BuildConfig
import com.spaceship.netblocker.utils.Slog



fun getApp(port: Int, isIpv4: Boolean = true): AppInfo? {
    AppInfoGetter.getAppInfo(port, isIpv4)?.let {
        return it
    } ?: run {
        return AppInfoGetter.getAppInfo(port, !isIpv4)
    }
}

fun getPackageNameByPort(port: Short, isIpv4: Boolean = true): String? {
    fun get(isIpv4: Boolean): String? {
        return try {
            AppInfoGetter.getPackageName(BitOperationUtils.shortToInt(port), isIpv4)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Slog.w("", "getPackageNameByPort fail:${e.message}")
            }
            null
        }
    }
    return get(isIpv4) ?: get(!isIpv4)
}

fun getApp(short: Short, isIpv4: Boolean = true): AppInfo? {
    return getApp(BitOperationUtils.shortToInt(short), isIpv4)
}

fun printApp(tag: String, port: Int) {
    printAppInner(tag, port, getApp(port)?.packageName)
}

fun printApp(tag: String, port: Short) {
    printAppInner(tag, port, getApp(port)?.packageName)
}


fun printAppInner(tag: String, port: Any, app: String?) {
    val pp = if (port is Short) {
        BitOperationUtils.shortToInt(port)
    } else {
        port
    }
    val msg = "$tag & port=$pp ---> $app"
    if (app != null) {
        Slog.v("Get App", msg)
    }
}
