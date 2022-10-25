package com.spaceship.netblocker.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.spaceship.netblocker.vpn_v28.shadowsocks.core.NatSession;

import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.UdpPacket;

/**
 * @author John
 * @since 2019-12-13 21:56
 */
public class DispatchPacket {

    @SerializedName("domain")
    public String domain;
    @SerializedName("port")
    public short port;
    @SerializedName("startTime")
    public long startTime;
    @SerializedName("pkg")
    public String pkg;
    @SerializedName("isIpv4")
    public boolean isIpv4;

    public DispatchPacket(IpPacket packet, String domain) {
        startTime = System.currentTimeMillis();
        this.domain = domain;
        port = getPort(packet);
        isIpv4 = !(packet instanceof IpV6Packet);
    }

    public DispatchPacket(NatSession session) {
        startTime = session.getStartTime();
        if (!TextUtils.isEmpty(session.getRemoteHost())) {
            this.domain = session.getRemoteHost();
        } else if (!TextUtils.isEmpty(session.getUrl())) {
            this.domain = session.getUrl();
        }
        port = session.getLocalPort();
        isIpv4 = true;
    }

    private short getPort(IpPacket packet) {
        try {
            return ((UdpPacket.UdpHeader) packet.getPayload().getHeader()).getSrcPort().value();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "DispatchPacket{" +
                "domain='" + domain + '\'' +
                ", port=" + port +
                ", startTime=" + startTime +
                ", pkg='" + pkg + '\'' +
                ", isIpv4=" + isIpv4 +
                '}';
    }
}
