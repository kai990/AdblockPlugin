package com.spaceship.netblocker.vpn_v28.shadowsocks.core;

import com.spaceship.netblocker.vpn_v28.shadowsocks.tunnel.Config;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tunnel.RawTunnel;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tunnel.Tunnel;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tunnel.httpconnect.HttpConnectConfig;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tunnel.httpconnect.HttpConnectTunnel;

import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

class TunnelFactory {

    static Tunnel wrap(SocketChannel channel, Selector selector) {
        return new RawTunnel(channel, selector);
    }

    static Tunnel createTunnelByConfig(InetSocketAddress destAddress, Selector selector) throws Exception {
        if (destAddress.isUnresolved()) {
            Config config = ProxyConfig.INSTANCE.getDefaultTunnelConfig();
            if (config instanceof HttpConnectConfig) {
                return new HttpConnectTunnel((HttpConnectConfig) config, selector);
            } else {
                return null;
            }
        } else {
            return new RawTunnel(destAddress, selector);
        }
    }

}
