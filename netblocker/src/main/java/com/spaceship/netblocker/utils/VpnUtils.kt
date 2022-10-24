package com.spaceship.netblocker.utils

import com.spaceship.netblocker.NetBlocker

/**
 * @author wangkai
 */

fun isVpnConnected(): Boolean {
    return NetBlocker.isVpnConnected()
}
