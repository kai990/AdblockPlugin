package com.spaceship.netblocker.core

import android.annotation.SuppressLint
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import com.spaceship.netblocker.NetBlocker
import com.spaceship.netblocker.utils.Slog



private const val TAG = "VpnServiceUtils"

@SuppressLint("PrivateApi")
internal fun establish(service: VpnService): ParcelFileDescriptor {
    val config = NetBlocker.vpnConfig

    val builder = service.Builder().apply {
        setMtu(config.mtu)
        addAddress(config.address.first, config.address.second)
        setSession(config.sessionName)

        setBlocking(true)
        allowFamily(OsConstants.AF_INET)
        allowFamily(OsConstants.AF_INET6)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setMetered(false)
        }
    }

    // dns
    for (dns in config.dnsList) {
        builder.addDnsServer(dns)
    }

    builder.addRoute("0.0.0.0", 0)

    val properties = Class.forName("android.os.SystemProperties")
    val method = properties.getMethod("get", String::class.java)
    val servers = ArrayList<String>()
    for (name in arrayOf("net.dns1", "net.dns2", "net.dns3", "net.dns4")) {
        val value = method.invoke(null, name) as String
        if (value.isNotEmpty() && !servers.contains(value)) {
            servers.add(value)
            if (value.replace("\\d".toRegex(), "").length == 3) {
                builder.addRoute(value, 32)
            } else {
                builder.addRoute(value, 128)
            }
        }
    }

    with(NetBlocker.blockConfig) {
        if (allowedAppSet.isEmpty()) {
            return@with
        }


        builder.addAllowedApplication(packageId)
        Slog.d(TAG, "vpn pkg list:${allowedAppSet}")
        for (pkg in allowedAppSet) {
            builder.addAllowedApplication(pkg)
        }
    }

    if (config.configureIntent != null) {
        builder.setConfigureIntent(config.configureIntent)
    }
    return builder.establish() ?: throw RuntimeException("connect error")
}
