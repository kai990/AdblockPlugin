package com.spaceship.netblocker.vpn_v28.core;

import static com.spaceship.netblocker.vpn.AdVpnService.VPN_STATUS_RUNNING;
import static com.spaceship.netblocker.vpn.AdVpnService.VPN_STATUS_STARTING;
import static com.spaceship.netblocker.vpn.AdVpnService.VPN_STATUS_STOPPED;
import static com.spaceship.netblocker.vpn.AdVpnService.VPN_UPDATE_STATUS_EXTRA;
import static com.spaceship.netblocker.vpn.AdVpnService.VPN_UPDATE_STATUS_INTENT;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.spaceship.netblocker.NetBlocker;
import com.spaceship.netblocker.VpnForegroundNotification;
import com.spaceship.netblocker.VpnRequestActivity;
import com.spaceship.netblocker.core.VpnServiceUtilsKt;
import com.spaceship.netblocker.utils.ContextUtilsKt;
import com.spaceship.netblocker.utils.Slog;
import com.spaceship.netblocker.vpn_v28.shadowsocks.core.DnsProxyManager;
import com.spaceship.netblocker.vpn_v28.shadowsocks.core.TcpProxyManager;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.CommonMethods;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.IPHeader;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.TCPHeader;
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.UDPHeader;
import com.spaceship.netblocker.utils.thread.ThreadPool;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class LocalVpnService extends VpnService {

    private static final String TAG = "VpnServiceTmp";

    private static final String COMMAND_START = "command_start";
    private static final String COMMAND_STOP = "command_stop";

    public static LocalVpnService INSTANCE;
    private volatile static int state = VPN_STATUS_STOPPED;
    private ParcelFileDescriptor fileDescriptor;
    private TcpProxyManager tcpProxyManager;
    private DnsProxyManager dnsProxyManager;
    private FileOutputStream outputStream;
    private byte[] packet;
    private IPHeader ipHeader;
    private TCPHeader tcpHeader;
    private UDPHeader udpHeader;
    private ByteBuffer dnsBuffer;
    private PacketDispatchHelper packetHelper;
    private volatile boolean isRunning = false;

    public LocalVpnService() {
        packet = new byte[20000];
        ipHeader = new IPHeader(packet, 0);
        tcpHeader = new TCPHeader(packet, 20);
        udpHeader = new UDPHeader(packet, 20);
        dnsBuffer = ((ByteBuffer) ByteBuffer.wrap(packet).position(28)).slice();
    }

    public static void start(Context context) {
        try {
            Intent intent = new Intent(context, LocalVpnService.class);
            intent.setAction(COMMAND_START);
            ContextCompat.startForegroundService(context, intent);
        } catch (Exception e) {
            Slog.INSTANCE.e(e, true);
        }
    }

    public static void stop(Context context) {
        try {
            Intent intent = new Intent(context, LocalVpnService.class);
            intent.setAction(COMMAND_STOP);
            ContextCompat.startForegroundService(context, intent);
        } catch (Exception e) {
            Slog.INSTANCE.e(e, true);
        }
    }

    public static void sendUdpPacket(IPHeader ipHeader, UDPHeader udpHeader) {
        try {
            CommonMethods.ComputeUDPChecksum(ipHeader, udpHeader);
            INSTANCE.outputStream.write(ipHeader.data, ipHeader.offset, ipHeader.getTotalLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isConnected() {
        return state == VPN_STATUS_RUNNING && ContextUtilsKt.isServiceActive(LocalVpnService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        VpnForegroundNotification vpnNotification = NetBlocker.INSTANCE.getNotification();
        startForeground(vpnNotification.id(), vpnNotification.notification());
    }

    private boolean prepareEnv() {
        if (prepare(this) != null) {
            startActivity(new Intent(this, VpnRequestActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            return false;
        }
        resetManager();
        tcpProxyManager.start();
        dnsProxyManager.start();
        return true;
    }

    private void resetManager() {
        try {
            tcpProxyManager = new TcpProxyManager(0);
            dnsProxyManager = new DnsProxyManager();
        } catch (IOException e) {
            Slog.INSTANCE.e(e, true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(COMMAND_START)) {
                stopConnect();
                VpnForegroundNotification vpnNotification = NetBlocker.INSTANCE.getNotification();
                startForeground(vpnNotification.id(), vpnNotification.notification());
                ThreadPool.INSTANCE.io(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            state = VPN_STATUS_STARTING;
                            if (prepareEnv()) {
                                startVpn();
                            }
                        } catch (Exception e) {
                            Slog.INSTANCE.e(e, true);
                        }
                    }
                });
            } else if (action.equals(COMMAND_STOP)) {
                stopVpn();
            }
        }
        return START_STICKY;
    }

    private void stopVpn() {
        isRunning = false;
        if (!isConnected()) {
            return;
        }

        stopConnect();

        onConnectChange(VPN_STATUS_STOPPED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopSelf();
        }
    }

    private void stopConnect() {
        try {
            if (dnsProxyManager != null) {
                dnsProxyManager.stop();
            }
            if (tcpProxyManager != null) {
                tcpProxyManager.stop();
            }
            try {
                if (fileDescriptor != null) {
                    fileDescriptor.close();
                }
            } catch (Exception e) {
                Slog.INSTANCE.e(e, true);
            } finally {
                fileDescriptor = null;
            }
        } catch (Exception e) {
            Slog.INSTANCE.e(e, true);
        }
    }

    private void handlePacket(IPHeader ipHeader, int size) {
        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                packetHelper.dispatchTcp(size);
                break;
            case IPHeader.UDP:
                packetHelper.dispatchUdp(dnsBuffer);
                break;
        }
    }

    private void startVpn() throws Exception {
        onConnectChange(VPN_STATUS_STARTING);
        fileDescriptor = VpnServiceUtilsKt.establish(this);
        onConnectChange(VPN_STATUS_RUNNING);
        this.outputStream = new FileOutputStream(fileDescriptor.getFileDescriptor());
        FileInputStream in = new FileInputStream(fileDescriptor.getFileDescriptor());
        packetHelper = new PacketDispatchHelper(ipHeader, tcpHeader, udpHeader, tcpProxyManager, dnsProxyManager, outputStream);
        isRunning = true;
        int size = 0;
        while (size != -1) {
            while ((size = in.read(packet)) > 0 && isRunning) {
                if (dnsProxyManager.isStop || tcpProxyManager.isStoped) {
                    in.close();
                    throw new Exception("LocalServer stopped.");
                }
                handlePacket(ipHeader, size);
            }
            Thread.sleep(20);
        }
        in.close();
    }

    private void onConnectChange(int state) {
        LocalVpnService.state = state;
        Intent intent = new Intent(VPN_UPDATE_STATUS_INTENT);
        intent.putExtra(VPN_UPDATE_STATUS_EXTRA, state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        Slog.INSTANCE.w(TAG, "onDestroy");
//        stopConnect();
        onConnectChange(VPN_STATUS_STOPPED);
        super.onDestroy();
    }
}
