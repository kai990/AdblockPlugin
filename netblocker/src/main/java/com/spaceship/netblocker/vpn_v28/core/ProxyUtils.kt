package com.spaceship.netblocker.vpn_v28.core

import com.spaceship.netblocker.NetBlocker
import com.spaceship.netblocker.ProxyDispatcher
import com.spaceship.netblocker.model.DispatchPacket
import com.spaceship.netblocker.vpn_v28.shadowsocks.core.NatSession
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.CommonMethods

/**
 * @author wangkai
 */
private val FAKE_NETWORK_MASK = CommonMethods.ipStringToInt("255.255.0.0")
private val FAKE_NETWORK_IP = CommonMethods.ipStringToInt("172.25.0.0")
const val DNS_TTL = 60
private val USER_AGENT by lazy { System.getProperty("http.agent") }

fun isFakeIP(ip: Int): Boolean {
    return ip and FAKE_NETWORK_MASK == FAKE_NETWORK_IP
}

fun getFakeIp(hashIP: Int) = FAKE_NETWORK_IP or (hashIP and 0x0000FFFF)

fun dispatchDomain(session: NatSession): Int {
    if (isFakeIP(session.remoteIP)) {
        return ProxyDispatcher.TYPE_DIRECT
    }
    return NetBlocker.dispatchDomain(DispatchPacket(session))
}

fun getUserAgent(): String = USER_AGENT.orEmpty()
