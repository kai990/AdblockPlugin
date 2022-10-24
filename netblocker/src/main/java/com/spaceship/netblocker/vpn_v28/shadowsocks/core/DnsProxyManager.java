package com.spaceship.netblocker.vpn_v28.shadowsocks.core;

import android.util.SparseArray;

import com.spaceship.netblocker.utils.VpnLogKt;
import com.spaceship.netblocker.vpn_v28.core.LocalVpnService;
import com.spaceship.netblocker.vpn_v28.shadowsocks.dns.DnsPacket;
import com.spaceship.netblocker.vpn_v28.shadowsocks.dns.Question;
import com.spaceship.netblocker.vpn_v28.shadowsocks.dns.Resource;
import com.spaceship.netblocker.vpn_v28.shadowsocks.dns.ResourcePointer;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.CommonMethods;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.IPHeader;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.UDPHeader;
import com.spaceship.netblocker.utils.Slog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public class DnsProxyManager implements Runnable {

    private static final ConcurrentHashMap<Integer, String> IP_TO_DOMAIN = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> DOMAIN_TO_IP = new ConcurrentHashMap<>();
    private final SparseArray<QueryState> queryArray;
    public boolean isStop;
    private DatagramSocket socket;
    private short queryId;

    public DnsProxyManager() throws IOException {
        queryArray = new SparseArray<>();
        socket = new DatagramSocket(0);
    }

    static String reverseLookup(int ip) {
        return IP_TO_DOMAIN.get(ip);
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setName("DnsProxyThread");
        thread.start();
    }

    public void stop() {
        isStop = true;
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    @Override
    public void run() {
        try {
            byte[] RECEIVE_BUFFER = new byte[2000];
            IPHeader ipHeader = new IPHeader(RECEIVE_BUFFER, 0);
            ipHeader.Default();
            UDPHeader udpHeader = new UDPHeader(RECEIVE_BUFFER, 20);

            ByteBuffer dnsBuffer = ByteBuffer.wrap(RECEIVE_BUFFER);
            dnsBuffer.position(28);
            dnsBuffer = dnsBuffer.slice();

            DatagramPacket packet = new DatagramPacket(RECEIVE_BUFFER, 28, RECEIVE_BUFFER.length - 28);

            while (socket != null && !socket.isClosed()) {

                packet.setLength(RECEIVE_BUFFER.length - 28);
                socket.receive(packet);

                dnsBuffer.clear();
                dnsBuffer.limit(packet.getLength());
                try {
                    DnsPacket dnsPacket = DnsPacket.fromBytes(dnsBuffer);
                    if (dnsPacket != null) {
                        OnDnsResponseReceived(ipHeader, udpHeader, dnsPacket);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    VpnLogKt.writeLog("Parse dns error: %s", e);
                }
            }
        } catch (Exception e) {
            Slog.INSTANCE.e(e, true);
        } finally {
            Slog.INSTANCE.w("DnsResolver Thread Exited.");
            this.stop();
        }
    }

    private int getFirstIP(DnsPacket dnsPacket) {
        for (int i = 0; i < dnsPacket.header.resourceCount; i++) {
            Resource resource = dnsPacket.resources[i];
            if (resource.type == 1) {
                return CommonMethods.readInt(resource.data, 0);
            }
        }
        return 0;
    }

    private void tamperDnsResponse(byte[] rawPacket, DnsPacket dnsPacket, int newIP) {
        Question question = dnsPacket.questions[0];

        dnsPacket.header.setResourceCount((short) 1);
        dnsPacket.header.setAResourceCount((short) 0);
        dnsPacket.header.seteResourceCount((short) 0);

        ResourcePointer rPointer = new ResourcePointer(rawPacket, question.offset() + question.length());
        rPointer.setDomain((short) 0xC00C);
        rPointer.setType(question.type);
        rPointer.setClass(question.clz);
        rPointer.setTTL(ProxyConfig.INSTANCE.getDnsTTL());
        rPointer.setDataLength((short) 4);
        rPointer.setIP(newIP);

        dnsPacket.size = 12 + question.length() + 16;
    }

    private int getOrCreateFakeIP(String domainString) {
        Integer fakeIP = DOMAIN_TO_IP.get(domainString);
        if (fakeIP == null) {
            int hashIP = domainString.hashCode();
            do {
                fakeIP = ProxyConfig.FAKE_NETWORK_IP | (hashIP & 0x0000FFFF);
                hashIP++;
            } while (IP_TO_DOMAIN.containsKey(fakeIP));

            DOMAIN_TO_IP.put(domainString, fakeIP);
            IP_TO_DOMAIN.put(fakeIP, domainString);
        }
        return fakeIP;
    }

    private void OnDnsResponseReceived(IPHeader ipHeader, UDPHeader udpHeader, DnsPacket dnsPacket) {
        QueryState state;
        synchronized (queryArray) {
            state = queryArray.get(dnsPacket.header.id);
            if (state != null) {
                queryArray.remove(dnsPacket.header.id);
            }
        }

        if (state != null) {
            //DNS污染，默认污染海外网站
//            dnsPollution(udpHeader.data, dnsPacket);

            dnsPacket.header.setId(state.clientQueryID);
            ipHeader.setSourceIP(state.remoteIP);
            ipHeader.setDestinationIP(state.clientIP);
            ipHeader.setProtocol(IPHeader.UDP);
            ipHeader.setTotalLength(20 + 8 + dnsPacket.size);
            udpHeader.setSourcePort(state.remotePort);
            udpHeader.setDestinationPort(state.clientPort);
            udpHeader.setTotalLength(8 + dnsPacket.size);

            LocalVpnService.sendUdpPacket(ipHeader, udpHeader);
        }
    }

    private int getIPFromCache(String domain) {
        Integer ip = DOMAIN_TO_IP.get(domain);
        if (ip == null) {
            return 0;
        } else {
            return ip;
        }
    }

    private boolean interceptDns(IPHeader ipHeader, UDPHeader udpHeader, DnsPacket dnsPacket) {
//        Question question = dnsPacket.questions[0];
//        System.out.println("DNS Qeury " + question.domain);
//        if (question.type == 1) {
//            if (ProxyConfig.INSTANCE.needProxy(question.domain, getIPFromCache(question.domain))) {
//                int fakeIP = getOrCreateFakeIP(question.domain);
//                tamperDnsResponse(ipHeader.data, dnsPacket, fakeIP);
//
//                if (ProxyConfig.IS_DEBUG)
//                    System.out.printf("interceptDns FakeDns: %s=>%s\n", question.domain, CommonMethods.ipIntToString(fakeIP));
//
//                int sourceIP = ipHeader.getSourceIP();
//                short sourcePort = udpHeader.getSourcePort();
//                ipHeader.setSourceIP(ipHeader.getDestinationIP());
//                ipHeader.setDestinationIP(sourceIP);
//                ipHeader.setTotalLength(20 + 8 + dnsPacket.size);
//                udpHeader.setSourcePort(udpHeader.getDestinationPort());
//                udpHeader.setDestinationPort(sourcePort);
//                udpHeader.setTotalLength(8 + dnsPacket.size);
//                LocalVpnService.sendUdpPacket(ipHeader, udpHeader);
//                return true;
//            }
//        }
        return false;
    }

    private void clearExpiredQueries() {
        long now = System.nanoTime();
        for (int i = queryArray.size() - 1; i >= 0; i--) {
            QueryState state = queryArray.valueAt(i);
            long QUERY_TIMEOUT_NS = 10 * 1000000000L;
            if ((now - state.queryNanoTime) > QUERY_TIMEOUT_NS) {
                queryArray.removeAt(i);
            }
        }
    }

    public void onDnsRequestReceived(IPHeader ipHeader, UDPHeader udpHeader, DnsPacket dnsPacket) {
        if (!interceptDns(ipHeader, udpHeader, dnsPacket)) {
            //转发DNS
            QueryState state = new QueryState();
            state.clientQueryID = dnsPacket.header.id;
            state.queryNanoTime = System.nanoTime();
            state.clientIP = ipHeader.getSourceIP();
            state.clientPort = udpHeader.getSourcePort();
            state.remoteIP = ipHeader.getDestinationIP();
            state.remotePort = udpHeader.getDestinationPort();

            // 转换QueryID
            queryId++;// 增加ID
            dnsPacket.header.setId(queryId);

            synchronized (queryArray) {
                clearExpiredQueries();//清空过期的查询，减少内存开销。
                queryArray.put(queryId, state);// 关联数据
            }

            InetSocketAddress remoteAddress = new InetSocketAddress(CommonMethods.ipIntToInet4Address(state.remoteIP), state.remotePort);
            DatagramPacket packet = new DatagramPacket(udpHeader.data, udpHeader.offset + 8, dnsPacket.size);
            packet.setSocketAddress(remoteAddress);

            try {
                if (LocalVpnService.INSTANCE.protect(socket)) {
                    socket.send(packet);
                } else {
                    System.err.println("VPN protect udp socket failed.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class QueryState {
        short clientQueryID;
        long queryNanoTime;
        int clientIP;
        short clientPort;
        int remoteIP;
        short remotePort;
    }
}
