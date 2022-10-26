package com.spaceship.netblocker.vpn_v28.shadowsocks.core

import android.util.SparseArray
import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.CommonMethods
import com.spaceship.netblocker.utils.Slog

object NatSessionManager {

    private const val MAX_SESSION_COUNT = 500
    private const val SESSION_TIMEOUT_MS = 120 * 1000L
    private val sessions = SparseArray<NatSession>()

    fun getSession(portKey: Int): NatSession? {
        val session = sessions.get(portKey)
        if (session != null) {
            session.endTime = System.currentTimeMillis()
        }
        return sessions.get(portKey)
    }

    private fun clearExpiredSessions() {
        try {
            val now = System.currentTimeMillis()
            for (i in sessions.size() - 1 downTo 0) {
                val session = sessions.valueAt(i)
                if (now - session.endTime > SESSION_TIMEOUT_MS) {
                    sessions.removeAt(i)
                }
            }
        } catch (e: Exception) {
            Slog.e(e)
        }

    }

    fun createSession(portKey: Int, remoteIP: Int, remotePort: Short, protocol: Int): NatSession {
        if (sessions.size() > MAX_SESSION_COUNT) {
            clearExpiredSessions()
        }

        val session = NatSession()
        session.startTime = System.currentTimeMillis()
        session.endTime = session.startTime
        session.remoteIP = remoteIP
        session.remotePort = remotePort
        session.protocol = protocol

        if (ProxyConfig.isFakeIP(remoteIP)) {
            session.remoteHost = DnsProxyManager.reverseLookup(remoteIP)
        }

        if (session.remoteHost.isNullOrEmpty()) {
            session.remoteHost = CommonMethods.ipIntToString(remoteIP)
        }
        sessions.put(portKey, session)
        return session
    }
}
