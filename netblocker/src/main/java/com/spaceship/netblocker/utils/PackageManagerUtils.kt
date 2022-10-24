package com.spaceship.netblocker.utils

import android.content.Context
import android.content.pm.PackageManager
import com.spaceship.netblocker.Env

/**
 * @author John
 * @since 2019-06-02 11:39
 */
fun getPackageInfo(context: Context, uid: Int?): AppInfo? {
    val manager = context.packageManager
    return getPackageInfo(manager, uid)
}

fun getPackageNameByUid(uid: Int?): String? {
    val manager = Env.getApp().packageManager
    uid?.let {
        return manager.getPackagesForUid(uid)?.getOrNull(0)
    }
    return null
}

private fun getPackageInfo(manager: PackageManager, uid: Int?): AppInfo? {
    uid?.let {
        val packagesForUid = manager.getPackagesForUid(uid)
        if (packagesForUid != null) {
            val packageName = packagesForUid[0]
            val pInfo = manager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            return AppInfo(packageName, version)
        }
    }
    return null
}