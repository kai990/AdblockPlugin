/* Copyright (C) 2016-2019 Julian Andres Klode <jak@jak-linux.org>
 *
 * Derived from AdBuster:
 * Copyright (C) 2016 Daniel Brodie <dbrodie@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Contributions shall also be provided under any later versions of the
 * GPL.
 */
package com.spaceship.netblocker.vpn;

import android.content.Context;

import com.spaceship.netblocker.NetBlocker;
import com.spaceship.netblocker.ProxyDispatcher;
import com.spaceship.netblocker.model.DispatchPacket;
import com.spaceship.netblocker.utils.Slog;

import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.IpSelector;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV6Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.UdpPacket;
import org.pcap4j.packet.UnknownPacket;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.SOARecord;
import org.xbill.DNS.Section;
import org.xbill.DNS.TextParseException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Creates and parses packets, and sends packets to a remote socket or the device using
 * {@link AdVpnThread}.
 */
public class DnsPacketProxy {

    private static final String TAG = "DnsPacketProxy";
    // Choose a value that is smaller than the time needed to unblock a host.
    private static final int NEGATIVE_CACHE_TTL_SECONDS = 5;
    private static final SOARecord NEGATIVE_CACHE_SOA_RECORD;

    static {
        try {
            // Let's use a guaranteed invalid hostname here, clients are not supposed to use
            // our fake values, the whole thing just exists for negative caching.
            Name name = new Name("dns66.dns66.invalid.");
            NEGATIVE_CACHE_SOA_RECORD = new SOARecord(name, DClass.IN, NEGATIVE_CACHE_TTL_SECONDS,
                    name, name, 0, 0, 0, 0, NEGATIVE_CACHE_TTL_SECONDS);
        } catch (TextParseException e) {
            throw new RuntimeException(e);
        }
    }

    private final EventLoop eventLoop;
    private ArrayList<InetAddress> upstreamDnsServers = new ArrayList<>();

    DnsPacketProxy(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
    }

    /**
     * Initializes the rules database and the list of upstream servers.
     *
     * @param context            The context we are operating in (for the database)
     * @param upstreamDnsServers The upstream DNS servers to use; or an empty list if no
     *                           rewriting of ip addresses takes place
     * @throws InterruptedException If the database initialization was interrupted
     */
    void initialize(Context context, ArrayList<InetAddress> upstreamDnsServers) throws InterruptedException {
        this.upstreamDnsServers = upstreamDnsServers;
    }

    /**
     * Handles a responsePayload from an upstream DNS server
     *
     * @param requestPacket   The original request packet
     * @param responsePayload The payload of the response
     */
    void handleDnsResponse(IpPacket requestPacket, byte[] responsePayload) {
        UdpPacket udpOutPacket = (UdpPacket) requestPacket.getPayload();
        UdpPacket.Builder payLoadBuilder = new UdpPacket.Builder(udpOutPacket)
                .srcPort(udpOutPacket.getHeader().getDstPort())
                .dstPort(udpOutPacket.getHeader().getSrcPort())
                .srcAddr(requestPacket.getHeader().getDstAddr())
                .dstAddr(requestPacket.getHeader().getSrcAddr())
                .correctChecksumAtBuild(true)
                .correctLengthAtBuild(true)
                .payloadBuilder(
                        new UnknownPacket.Builder()
                                .rawData(responsePayload)
                );


        IpPacket ipOutPacket;
        if (requestPacket instanceof IpV4Packet) {
            ipOutPacket = new IpV4Packet.Builder((IpV4Packet) requestPacket)
                    .srcAddr((Inet4Address) requestPacket.getHeader().getDstAddr())
                    .dstAddr((Inet4Address) requestPacket.getHeader().getSrcAddr())
                    .correctChecksumAtBuild(true)
                    .correctLengthAtBuild(true)
                    .payloadBuilder(payLoadBuilder)
                    .build();

        } else {
            ipOutPacket = new IpV6Packet.Builder((IpV6Packet) requestPacket)
                    .srcAddr((Inet6Address) requestPacket.getHeader().getDstAddr())
                    .dstAddr((Inet6Address) requestPacket.getHeader().getSrcAddr())
                    .correctLengthAtBuild(true)
                    .payloadBuilder(payLoadBuilder)
                    .build();
        }

        eventLoop.queueDeviceWrite(ipOutPacket);
    }

    /**
     * Handles a DNS request, by either blocking it or forwarding it to the remote location.
     *
     * @param packetData The packet data to read
     * @throws AdVpnThread.VpnNetworkException If some network error occurred
     */
    void handleDnsRequest(byte[] packetData) throws AdVpnThread.VpnNetworkException {

        IpPacket parsedPacket = null;
        try {
            parsedPacket = (IpPacket) IpSelector.newPacket(packetData, 0, packetData.length);
        } catch (Exception e) {
            Slog.INSTANCE.i("handleDnsRequest: Discarding invalid IP packet", e);
            return;
        }

        UdpPacket parsedUdp;
        Packet udpPayload;

        try {
            parsedUdp = (UdpPacket) parsedPacket.getPayload();
            udpPayload = parsedUdp.getPayload();
        } catch (Exception e) {
            try {
                Slog.INSTANCE.e(e, false);
                Slog.INSTANCE.w(TAG, "handleDnsRequest: Discarding unknown packet type " + parsedPacket.getHeader());
            } catch (Exception e1) {
                Slog.INSTANCE.e(e1, false);
                Slog.INSTANCE.w(TAG, "handleDnsRequest: Discarding unknown packet type, could not log packet info");
            }
            return;
        }

        InetAddress destAddr = translateDestinationAdress(parsedPacket);
        if (destAddr == null)
            return;

        if (udpPayload == null) {
            try {
                Slog.INSTANCE.i(TAG, "handleDnsRequest: Sending UDP packet without payload: " + parsedUdp);
            } catch (Exception e1) {
                Slog.INSTANCE.i(TAG, "handleDnsRequest: Sending UDP packet without payload");
            }

            // Let's be nice to Firefox. Firefox uses an empty UDP packet to
            // the gateway to reduce the RTT. For further details, please see
            // https://bugzilla.mozilla.org/show_bug.cgi?id=888268
            try {
                DatagramPacket outPacket = new DatagramPacket(new byte[0], 0, 0 /* length */, destAddr, parsedUdp.getHeader().getDstPort().valueAsInt());
                eventLoop.forwardPacket(outPacket, null);
            } catch (Exception e) {
                Slog.INSTANCE.e(e, false);
                Slog.INSTANCE.w(TAG, "handleDnsRequest: Could not send empty UDP packet");
            }
            return;
        }

        byte[] dnsRawData = udpPayload.getRawData();
        Message dnsMsg;
        try {
            dnsMsg = new Message(dnsRawData);
        } catch (IOException e) {
            Slog.INSTANCE.i("handleDnsRequest: Discarding non-DNS or invalid packet", e);
            return;
        }
        if (dnsMsg.getQuestion() == null) {
            Slog.INSTANCE.i(TAG, "handleDnsRequest: Discarding DNS packet with no query " + dnsMsg);
            return;
        }
        String dnsQueryName = dnsMsg.getQuestion().getName().toString(true);
        Slog.INSTANCE.v("ORIGIN", "domain:" + dnsQueryName);
        boolean isBlocked = NetBlocker.INSTANCE.dispatchDomain(new DispatchPacket(parsedPacket, dnsQueryName)) != ProxyDispatcher.TYPE_DIRECT;
        if (!isBlocked) {
            Slog.INSTANCE.i(TAG, "handleDnsRequest: DNS Name " + dnsQueryName + " Allowed, sending to " + destAddr);
            DatagramPacket outPacket = new DatagramPacket(dnsRawData, 0, dnsRawData.length, destAddr, parsedUdp.getHeader().getDstPort().valueAsInt());
            eventLoop.forwardPacket(outPacket, parsedPacket);
        } else {
            Slog.INSTANCE.i(TAG, "handleDnsRequest: DNS Name " + dnsQueryName + " Blocked!");
            dnsMsg.getHeader().setFlag(Flags.QR);
            dnsMsg.getHeader().setRcode(Rcode.NOERROR);
            dnsMsg.addRecord(NEGATIVE_CACHE_SOA_RECORD, Section.AUTHORITY);
            handleDnsResponse(parsedPacket, dnsMsg.toWire());
        }
    }

    /**
     * Translates the destination address in the packet to the real one. In
     * case address translation is not used, this just returns the original one.
     *
     * @param parsedPacket Packet to get destination address for.
     * @return The translated address or null on failure.
     */
    private InetAddress translateDestinationAdress(IpPacket parsedPacket) {
        InetAddress destAddr = null;
        if (upstreamDnsServers.size() > 0) {
            byte[] addr = parsedPacket.getHeader().getDstAddr().getAddress();
            int index = addr[addr.length - 1] - 2;

            try {
                destAddr = upstreamDnsServers.get(index);
            } catch (Exception e) {
                Slog.INSTANCE.e("handleDnsRequest: Cannot handle packets to" + parsedPacket.getHeader().getDstAddr().getHostAddress(), e);
                return null;
            }
            Slog.INSTANCE.d(TAG, String.format("handleDnsRequest: Incoming packet to %s AKA %d AKA %s", parsedPacket.getHeader().getDstAddr().getHostAddress(), index, destAddr));
        } else {
            destAddr = parsedPacket.getHeader().getDstAddr();
            Slog.INSTANCE.d(TAG, String.format("handleDnsRequest: Incoming packet to %s - is upstream", parsedPacket.getHeader().getDstAddr().getHostAddress()));
        }
        return destAddr;
    }

    /**
     * Interface abstracting away {@link AdVpnThread}.
     */
    interface EventLoop {
        /**
         * Called to send a packet to a remote location
         *
         * @param packet        The packet to send
         * @param requestPacket If specified, the event loop must wait for a response, and then
         *                      call {@link #handleDnsResponse(IpPacket, byte[])} for the data
         *                      of the response, with this packet as the first argument.
         */
        void forwardPacket(DatagramPacket packet, IpPacket requestPacket) throws AdVpnThread.VpnNetworkException;

        /**
         * Write an IP packet to the local TUN device
         *
         * @param packet The packet to write (a response to a DNS request)
         */
        void queueDeviceWrite(IpPacket packet);
    }
}
