
package com.spaceship.netblocker.vpn;

import androidx.annotation.NonNull;

import com.spaceship.netblocker.utils.Slog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;



class VpnWatchdog {
    private static final String TAG = "VpnWatchDog";

    // Polling is quadrupled on every success, and values range from 4s to 1h8m.
    private static final int POLL_TIMEOUT_START = 1000;
    private static final int POLL_TIMEOUT_END = 4096000;
    private static final int POLL_TIMEOUT_WAITING = 7000;
    private static final int POLL_TIMEOUT_GROW = 4;

    // Reconnect penalty ranges from 0s to 5s, in increments of 200 ms.
    private static final int INIT_PENALTY_START = 0;
    private static final int INIT_PENALTY_END = 5000;
    private static final int INIT_PENALTY_INC = 200;

    int initPenalty = INIT_PENALTY_START;
    int pollTimeout = POLL_TIMEOUT_START;

    // Information about when packets where received.
    long lastPacketSent = 0;
    long lastPacketReceived = 0;

    private boolean enabled = false;
    private InetAddress target;


    
    int getPollTimeout() {
        if (!enabled)
            return -1;
        if (lastPacketReceived < lastPacketSent)
            return POLL_TIMEOUT_WAITING;
        return pollTimeout;
    }

    
    void setTarget(InetAddress target) {
        this.target = target;
    }

    
    void initialize(boolean enabled) throws InterruptedException {
        Slog.INSTANCE.d(TAG, "initialize: Initializing watchdog");

        pollTimeout = POLL_TIMEOUT_START;
        lastPacketSent = 0;
        this.enabled = enabled;

        if (!enabled) {
            Slog.INSTANCE.d(TAG, "initialize: Disabled.");
            return;
        }

        if (initPenalty > 0) {
            Slog.INSTANCE.d(TAG, "init penalty: Sleeping for " + initPenalty + "ms");
            Thread.sleep(initPenalty);
        }
    }

    
    void handleTimeout() throws AdVpnThread.VpnNetworkException {
        if (!enabled)
            return;
        Slog.INSTANCE.d(TAG, "handleTimeout: Milliseconds elapsed between last receive and sent: "
                + (lastPacketReceived - lastPacketSent));
        // Receive really timed out.
        if (lastPacketReceived < lastPacketSent && lastPacketSent != 0) {
            initPenalty += INIT_PENALTY_INC;
            if (initPenalty > INIT_PENALTY_END)
                initPenalty = INIT_PENALTY_END;
            throw new AdVpnThread.VpnNetworkException("Watchdog timed out");
        }
        // We received a packet after sending it, so we can be more confident and grow our wait
        // time.
        pollTimeout *= POLL_TIMEOUT_GROW;
        if (pollTimeout > POLL_TIMEOUT_END)
            pollTimeout = POLL_TIMEOUT_END;


        sendPacket();
    }

    
    void handlePacket(byte[] packetData) {
        if (!enabled)
            return;

        Slog.INSTANCE.d(TAG, "handlePacket: Received packet of length " + packetData.length);
        lastPacketReceived = System.currentTimeMillis();
    }

    
    void sendPacket() throws AdVpnThread.VpnNetworkException {
        if (!enabled)
            return;

        Slog.INSTANCE.d(TAG, "sendPacket: Sending packet, poll timeout is " + pollTimeout);

        DatagramPacket outPacket = new DatagramPacket(new byte[0], 0, 0 , target, 53);
        try {
            DatagramSocket socket = newDatagramSocket();
            socket.send(outPacket);
            socket.close();
            lastPacketSent = System.currentTimeMillis();
        } catch (IOException e) {
            throw new AdVpnThread.VpnNetworkException("Received exception", e);
        }
    }

    @NonNull
    DatagramSocket newDatagramSocket() throws SocketException {
        return new DatagramSocket();
    }
}
