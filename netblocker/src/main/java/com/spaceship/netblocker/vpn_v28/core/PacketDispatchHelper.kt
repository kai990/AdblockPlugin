package com.spaceship.netblocker.vpn_v28.core

import com.spaceship.netblocker.BuildConfig
import com.spaceship.netblocker.ProxyDispatcher
import com.spaceship.netblocker.vpn.VpnConfig
import com.spaceship.netblocker.vpn_v28.shadowsocks.core.DnsProxyManager
import com.spaceship.netblocker.vpn_v28.shadowsocks.core.NatSession
import com.spaceship.netblocker.vpn_v28.shadowsocks.core.NatSessionManager
import com.spaceship.netblocker.vpn_v28.shadowsocks.core.TcpProxyManager
import com.spaceship.netblocker.vpn_v28.shadowsocks.dns.DnsPacket
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.CommonMethods
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.IPHeader
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.TCPHeader
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.UDPHeader
import com.spaceship.netblocker.vpn_v28.shadowsocks.utils.HttpRequestHeaderParser
import com.spaceship.netblocker.utils.Slog
import java.io.FileOutputStream
import java.nio.ByteBuffer


class PacketDispatchHelper(
    private val ipHeader: IPHeader,
    private val tcpHeader: TCPHeader,
    private val udpHeader: UDPHeader,
    private val tcpProxyManager: TcpProxyManager,
    private val dnsProxyManager: DnsProxyManager,
    private val outStream: FileOutputStream
) {

    fun dispatchTcp(size: Int) {
        tcpHeader.offset = ipHeader.headerLength
        if (ipHeader.sourceIP == VpnConfig.LOCAL_IP) {
            if (tcpHeader.sourcePort == tcpProxyManager.port) {
                val session = NatSessionManager.getSession(tcpHeader.destinationPort.toInt())

                ipHeader.sourceIP = ipHeader.destinationIP
                if (session != null) {
                    tcpHeader.sourcePort = session.remotePort
                } else {
                    Slog.v(
                        TAG,
                        String.format(
                            "NoSession: %s %s\n",
                            ipHeader.toString(),
                            tcpHeader.toString()
                        )
                    )
                }
                ipHeader.destinationIP = VpnConfig.LOCAL_IP
                CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader)
                val type = if (session == null) ProxyDispatcher.TYPE_DIRECT else dispatchDomain(session)
                if (type != ProxyDispatcher.TYPE_BLOCK) {
                    outStream.write(ipHeader.data, ipHeader.offset, size)
                } else if (type == ProxyDispatcher.TYPE_BLOCK) {
//                    outStream.close()
                }
            } else {
                val portKey = tcpHeader.sourcePort.toInt()
                var session = NatSessionManager.getSession(portKey)
                if (session == null || session.remoteIP != ipHeader.destinationIP || session.remotePort != tcpHeader.destinationPort) {
                    session = NatSessionManager.createSession(
                        portKey,
                        ipHeader.destinationIP,
                        tcpHeader.destinationPort,
                        NatSession.PROTOCOL_TCP
                    )
                }

                session.endTime = System.currentTimeMillis()

                session.packetSent++

                val tcpDataSize = ipHeader.dataLength - tcpHeader.headerLength
                if (session.packetSent == 2 && tcpDataSize == 0) {
                    return
                }


                if (session.bytesSent == 0L && tcpDataSize > 10) {
                    val dataOffset = tcpHeader.offset + tcpHeader.headerLength
                    HttpRequestHeaderParser.parseHttpRequestHeader(
                        session,
                        tcpHeader.data,
                        dataOffset,
                        tcpDataSize
                    )
                } else if (session.bytesSent > 0 && !session.isHttps && session.remoteHost.isNullOrEmpty()) {
                    val dataOffset = tcpHeader.offset + tcpHeader.headerLength
                    session.remoteHost = HttpRequestHeaderParser.getRemoteHost(
                        tcpHeader.data, dataOffset,
                        tcpDataSize
                    )
                    session.url = "http://" + session.remoteHost + "/" + session.urlPath
                }


                ipHeader.sourceIP = ipHeader.destinationIP
                ipHeader.destinationIP = VpnConfig.LOCAL_IP
                tcpHeader.destinationPort = tcpProxyManager.port

                CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader)
                outStream.write(ipHeader.data, ipHeader.offset, size)

                session.bytesSent += tcpDataSize
                session.endTime = System.currentTimeMillis()
            }
        }
    }


    fun dispatchUdp(dnsBuffer: ByteBuffer) {
        tcpHeader.sourcePort.let {
            val portKey = it.toInt()
            var session = NatSessionManager.getSession(portKey)
            if (session == null || session.remoteIP != ipHeader.destinationIP || session.remotePort != tcpHeader.destinationPort) {
                session = NatSessionManager.createSession(
                    portKey,
                    ipHeader.destinationIP,
                    tcpHeader.destinationPort,
                    NatSession.PROTOCOL_UDP
                )
                session.startTime = System.currentTimeMillis()
            }
            session.endTime = System.currentTimeMillis()
            session.packetSent++ //注意顺序
        }

        if (BuildConfig.DEBUG) {
            val session = NatSessionManager.getSession(tcpHeader.sourcePort.toInt())
            Slog.w("dispatchUdp", "udp session: ${session.toString()}")
        }

        // 转发DNS数据包：
        udpHeader.offset = ipHeader.headerLength
        if (ipHeader.sourceIP == VpnConfig.LOCAL_IP && udpHeader.destinationPort.toInt() == 53) {
            dnsBuffer.clear()
            dnsBuffer.limit(ipHeader.dataLength - 8)
            val dnsPacket = DnsPacket.fromBytes(dnsBuffer)
            if (dnsPacket != null && dnsPacket.header.questionCount > 0) {
                dnsProxyManager.onDnsRequestReceived(ipHeader, udpHeader, dnsPacket)
            }
        }
    }

    companion object {
        private const val TAG = "PacketDispatchHelper"
    }
}
