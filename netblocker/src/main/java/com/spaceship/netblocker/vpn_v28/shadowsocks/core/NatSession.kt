package com.spaceship.netblocker.vpn_v28.shadowsocks.core

import com.spaceship.netblocker.utils.getPackageNameByPort

class NatSession(
    
    var protocol: Int = 0,

    var remoteIP: Int = 0,
    var remotePort: Short = 0,
    var remoteHost: String? = null,

    var bytesSent: Long = 0,
    var packetSent: Int = 0,

    var bytesReceive: Long = 0,
    var packetReceive: Int = 0,

    
    var url: String? = null,

    
    var urlPath: String? = "",

    
    var method: String = "",

    
    var localPort: Short = 0,

    
    var endTime: Long = 0,

    
    var startTime: Long = 0,

    var isHttps: Boolean = false,

    var pkg: String? = null
) {

    
    fun dump() {
        if (pkg.isNullOrEmpty()) {
            pkg = getPackageNameByPort(localPort, true)
        }
    }

    override fun toString(): String {
        return "${pkg}:${url ?: remoteHost}"
    }

    companion object {

        
        const val PROTOCOL_TCP = 1
        const val PROTOCOL_UDP = 2
    }
}
