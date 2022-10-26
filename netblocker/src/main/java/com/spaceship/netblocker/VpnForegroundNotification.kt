package com.spaceship.netblocker

import android.app.Notification


interface VpnForegroundNotification {

    fun notification(): Notification

    fun id(): Int
}