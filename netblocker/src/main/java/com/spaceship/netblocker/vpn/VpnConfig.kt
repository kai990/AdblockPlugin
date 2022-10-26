package com.spaceship.netblocker.vpn

import android.app.PendingIntent
import com.spaceship.netblocker.NetBlocker
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.CommonMethods


data class VpnConfig(
    val configureIntent: PendingIntent? = null,
    val notification: VpnServiceNotification? = null,
    val sessionName: String,
    val mtu: Int = 20000,
    val address: Pair<String, Int> = Pair("10.8.0.2", 32),
    val dnsList: ArrayList<String> = arrayListOf(),
    val routeList: List<Pair<String, Int>> = arrayListOf(),
    val fakeIp: String = "172.25.0.0",
    val fakeMask: String = "255.255.0.0"
) {
    companion object {
        val LOCAL_IP = CommonMethods.ipStringToInt(NetBlocker.vpnConfig.address.first)
    }
}