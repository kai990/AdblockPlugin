package com.spaceship.netblocker.utils

import com.spaceship.netblocker.Env
import java.io.File


object AppInfoGetter {

    
    private val IPV4 = listOf("/proc/net/icmp", "/proc/net/tcp", "/proc/net/udp")

    
    private val IPV6 = listOf("/proc/net/icmp6", "/proc/net/tcp6", "/proc/net/udp6")

    private val SPLIT_REGEX = "\\s+".toRegex()

    fun getAppInfo(port: Int, isIpv4: Boolean): AppInfo? {
        val files = if (isIpv4) IPV4 else IPV6
        files.forEach { path ->
            val file = File(path)
            if (!file.exists()) {
                return@forEach
            }
            file.useLines { lines ->
                lines.forEachIndexed { index, str ->
                    if (index > 0) {
                        getIpData(port, str.trim().split(SPLIT_REGEX))?.let { uid ->
                            return getPackageInfo(Env.getApp(), uid)
                        }
                    }
                }
            }
        }
        return null
    }

    fun getPackageName(port: Int, isIpv4: Boolean): String? {
        val files = if (isIpv4) IPV4 else IPV6
        files.forEach { path ->
            val file = File(path)
            if (!file.exists()) {
                return@forEach
            }
            file.useLines { lines ->
                lines.forEachIndexed { index, str ->
                    if (index > 0) {
                        getIpData(port, str.trim().split(SPLIT_REGEX))?.let { uid ->
                            return getPackageNameByUid(uid)
                        }
                    }
                }
            }
        }
        return null
    }

    private fun getIpData(port: Int, source: List<String>): Int? {
        val logPort = source[1].split(":")[1].toInt(16)
        if (logPort == port) {
            return source[7].toInt()
        }
        return null
    }
}
