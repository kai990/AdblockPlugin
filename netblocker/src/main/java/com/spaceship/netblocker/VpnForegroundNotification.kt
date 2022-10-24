package com.spaceship.netblocker

import android.app.Notification

/**
 * @author John
 * @since 2019-06-16 10:04
 */
interface VpnForegroundNotification {

    fun notification(): Notification

    fun id(): Int
}