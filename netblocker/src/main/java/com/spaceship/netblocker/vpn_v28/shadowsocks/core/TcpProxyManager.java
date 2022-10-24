package com.spaceship.netblocker.vpn_v28.shadowsocks.core;

import com.spaceship.netblocker.ProxyDispatcher;
import com.spaceship.netblocker.vpn_v28.core.ProxyUtilsKt;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tunnel.Tunnel;
import com.spaceship.netblocker.utils.thread.ThreadPool;
import com.spaceship.netblocker.utils.Slog;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class TcpProxyManager implements Runnable {

    public boolean isStoped;
    public short port;

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public TcpProxyManager(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.port = (short) serverSocketChannel.socket().getLocalPort();
        System.out.printf("AsyncTcpServer listen on %d success.\n", this.port & 0xFFFF);
    }

    public void start() {
        Thread serverThread = new Thread(this);
        serverThread.setName("TcpProxyServerThread");
        serverThread.start();
    }

    public void stop() {
        this.isStoped = true;
        if (selector != null) {
            ThreadPool.INSTANCE.io(new Runnable() {
                @Override
                public void run() {
                    // fix anr https://play.google.com/console/u/0/developers/5138378281199867930/app/4975274167845973441/vitals/crashes/89ac49fd/details?installedFrom=PLAY_STORE&days=30
                    try {
                        Selector tmp = selector;
                        selector = null;
                        tmp.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if (serverSocketChannel != null) {
            ThreadPool.INSTANCE.io(new Runnable() {
                @Override
                public void run() {
                    // fix anr https://play.google.com/console/u/0/developers/5138378281199867930/app/4975274167845973441/vitals/crashes/89ac49fd/details?installedFrom=PLAY_STORE&days=30
                    try {
                        ServerSocketChannel tmp = serverSocketChannel;
                        serverSocketChannel = null;
                        tmp.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    dispatchSelection(keyIterator);
                }
            }
        } catch (Exception e) {
            Slog.INSTANCE.e(e, true);
        } finally {
            this.stop();
            Slog.INSTANCE.w("TcpServer thread exited.");
        }
    }

    private void dispatchSelection(Iterator<SelectionKey> keyIterator) {
        SelectionKey key = keyIterator.next();
        if (key.isValid()) {
            try {
                if (key.isReadable()) {
                    ((Tunnel) key.attachment()).onReadable(key);
                } else if (key.isWritable()) {
                    ((Tunnel) key.attachment()).onWritable(key);
                } else if (key.isConnectable()) {
                    ((Tunnel) key.attachment()).onConnectable();
                } else if (key.isAcceptable()) {
                    onAccepted();
                }
            } catch (Exception e) {
                Slog.INSTANCE.e(e, true);
            }
        }
        keyIterator.remove();
    }

    private void onAccepted() {
        Tunnel localTunnel = null;
        try {
            SocketChannel localChannel = serverSocketChannel.accept();
            localTunnel = TunnelFactory.wrap(localChannel, selector);

            InetSocketAddress destAddress = getDestAddress(localChannel);
            if (destAddress != null) {
                Tunnel remoteTunnel = TunnelFactory.createTunnelByConfig(destAddress, selector);
                remoteTunnel.setBrotherTunnel(localTunnel);//关联兄弟
                localTunnel.setBrotherTunnel(remoteTunnel);//关联兄弟
                remoteTunnel.connect(destAddress);//开始连接
            } else {
                // 拦截 || 不能处理的请求
                localChannel.close();
                localTunnel.dispose();
            }
        } catch (Throwable e) {
            Slog.INSTANCE.w("Error: remote socket create failed: %s", e.toString());
            if (localTunnel != null) {
                localTunnel.dispose();
            }
        }
    }

    private InetSocketAddress getDestAddress(SocketChannel localChannel) {
        short portKey = (short) localChannel.socket().getPort();
        NatSession session = NatSessionManager.INSTANCE.getSession(portKey);
        if (session != null) {
            session.setLocalPort(portKey);
//            session.dump();
            int type = ProxyUtilsKt.dispatchDomain(session);
            if (type == ProxyDispatcher.TYPE_DIRECT) {
                return new InetSocketAddress(localChannel.socket().getInetAddress(), session.getRemotePort() & 0xFFFF);
            } else if (type == ProxyDispatcher.TYPE_REMOTE) {
                return InetSocketAddress.createUnresolved(session.getRemoteHost(), session.getRemotePort() & 0xFFFF);
            }
        }
        return null;
    }
}
