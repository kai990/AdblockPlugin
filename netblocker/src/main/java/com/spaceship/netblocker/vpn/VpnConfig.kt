package com.spaceship.netblocker.vpn

import android.app.PendingIntent
import com.spaceship.netblocker.NetBlocker
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.CommonMethods

/**
 * @author wangkai
 */
data class VpnConfig(
    // 通知栏点击、配置跳转页面
    val configureIntent: PendingIntent? = null,
    val notification: VpnServiceNotification? = null,
    // 通知栏上显示的名称 xxx 已激活VPN
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