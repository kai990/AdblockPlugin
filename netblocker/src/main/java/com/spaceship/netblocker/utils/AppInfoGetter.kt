package com.spaceship.netblocker.utils

import com.spaceship.netblocker.Env
import java.io.File

/**
 * 根据 port 获取 package info
 * @author John
 * @since 2019-06-02 16:45
 */
object AppInfoGetter {

    /**
    sl  local_address rem_address   st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode
    0: 00000000:C375 00000000:0000 8A 00000000:00000000 00:00000000 00000000  1001        0 13041764 1 0000000000000000 100 0 0 10 0
    1: 0100007F:0438 00000000:0000 0A 00000000:00000000 00:00000000 00000000 10576        0 20045184 1 0000000000000000 100 0 0 10 0
    2: 00000000:C37A 00000000:0000 8A 00000000:00000000 00:00000000 00000000  1001        0 13041755 1 0000000000000000 100 0 0 10 0
    3: 00000000:C37B 00000000:0000 8A 00000000:00000000 00:00000000 00000000  1001        0 13041756 1 0000000000000000 100 0 0 10 0
     */
    private val IPV4 = listOf("/proc/net/icmp", "/proc/net/tcp", "/proc/net/udp")

    /**
    sl  local_address                         remote_address                        st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode
    0: 00000000000000000000000000000000:C375 00000000000000000000000000000000:0000 8A 00000000:00000000 00:00000000 00000000  1001        0 13041752 1 0000000000000000 100 0 0 10 0
    1: 00000000000000000000000000000000:A717 00000000000000000000000000000000:0000 8A 00000000:00000000 00:00000000 00000000  1001        0 40279 1 0000000000000000 100 0 0 10 0
    2: 00000000000000000000000000000000:AAF8 00000000000000000000000000000000:0000 8A 00000000:00000000 00:00000000 00000000  1001        0 40307 1 0000000000000000 100 0 0 10 0
    3: 00000000000000000000000000000000:9FF9 00000000000000000000000000000000:0000 8A 00000000:00000000 00:00000000 00000000  1001        0 40283 1 0000000000000000 100 0 0 10 0
     */
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
