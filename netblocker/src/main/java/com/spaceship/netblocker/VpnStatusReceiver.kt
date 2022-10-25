package com.spaceship.netblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spaceship.netblocker.message.sendVpnStatus
import com.spaceship.netblocker.vpn.AdVpnService

class VpnStatusReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val status = intent?.getIntExtra(AdVpnService.VPN_UPDATE_STATUS_EXTRA, AdVpnService.VPN_STATUS_STARTING)
            ?: AdVpnService.VPN_STATUS_STARTING
        sendVpnStatus(status)
    }
}