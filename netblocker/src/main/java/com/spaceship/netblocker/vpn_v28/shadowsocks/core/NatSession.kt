package com.spaceship.netblocker.vpn_v28.shadowsocks.core

import com.spaceship.netblocker.utils.getPackageNameByPort

class NatSession(
    /**
     * 请求协议，TCP | UDP
     */
    var protocol: Int = 0,

    var remoteIP: Int = 0,
    var remotePort: Short = 0,
    var remoteHost: String? = null,

    var bytesSent: Long = 0,
    var packetSent: Int = 0,

    var bytesReceive: Long = 0,
    var packetReceive: Int = 0,

    /**
     * 请求地址
     */
    var url: String? = null,

    /**
     * 请求头中的地址, [url] 除去域名部分
     */
    var urlPath: String? = "",

    /**
     * GET,POST...
     */
    var method: String = "",

    /**
     * 本地端口，可用来查找对应 APP
     */
    var localPort: Short = 0,

    /**
     * 请求结束时间：纳秒
     */
    var endTime: Long = 0,

    /**
     * 开始时间：纳秒
     */
    var startTime: Long = 0,

    var isHttps: Boolean = false,

    var pkg: String? = null
) {

    /**
     * 处理一些数据
     */
    fun dump() {
        if (pkg.isNullOrEmpty()) {
            pkg = getPackageNameByPort(localPort, true)
        }
    }

    override fun toString(): String {
        return "${pkg}:${url ?: remoteHost}"
    }

    companion object {

        /**
         * 请求协议
         */
        const val PROTOCOL_TCP = 1
        const val PROTOCOL_UDP = 2
    }
}
