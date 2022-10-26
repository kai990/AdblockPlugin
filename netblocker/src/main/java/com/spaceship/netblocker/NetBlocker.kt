package com.spaceship.netblocker

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.text.format.DateUtils
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.spaceship.netblocker.model.DispatchPacket
import com.spaceship.netblocker.notification.VpnNotification
import com.spaceship.netblocker.vpn.AdVpnService
import com.spaceship.netblocker.vpn.Command
import com.spaceship.netblocker.vpn.VpnConfig
import com.spaceship.netblocker.vpn_v28.core.LocalVpnService
import com.spaceship.netblocker.utils.thread.ThreadPool.uiDelay



object NetBlocker {
    private lateinit var context: Application

    val blockConfig by lazy { BlockConfig(context.packageName) }

    val vpnConfig by lazy { VpnConfig(sessionName = "") }

    private val notification by lazy { VpnNotification(context) }

    private val proxyDispatcher by lazy { ProxyDispatcher() }

    fun init(context: Application, packageId: String) {
        this.context = context
        blockConfig.packageId = packageId

        LocalBroadcastManager.getInstance(context)
            .registerReceiver(VpnStatusReceiver(), IntentFilter(AdVpnService.VPN_UPDATE_STATUS_INTENT))
    }

    fun isInited() = this::context.isInitialized

    fun startVpn() {
        startServiceInternal()
    }

    fun stopVpn() {
        stopServiceInternal()
    }

    fun restartVpn() {
        stopServiceInternal()
        uiDelay(2 * DateUtils.SECOND_IN_MILLIS) {
            startServiceInternal()
        }
    }

    
    fun setDispatchHandler(dispatchHandler: ProxyDispatcher.RequestDispatchHandler) {
        proxyDispatcher.setDispatchHandler(dispatchHandler)
    }

    fun getDispatchHandler(): ProxyDispatcher.RequestDispatchHandler? {
        return proxyDispatcher.getDispatchHandler()
    }

    
    fun dispatchDomain(packet: DispatchPacket): Int {
        return proxyDispatcher.dispatch(packet)
    }

    fun getNotification(): VpnForegroundNotification {
        return notification
    }

    
    fun setAllowedAppList(appList: List<String>) {
        blockConfig.allowedAppSet.addAll(appList)
    }

    fun isVpnConnected(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AdVpnService.vpnStatus == AdVpnService.VPN_STATUS_RUNNING
        } else {
            LocalVpnService.isConnected()
        }
    }

    fun setDnsServer(dnsList: List<String>) {
        vpnConfig.dnsList.addAll(dnsList)
    }

    private fun startServiceInternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(context, AdVpnService::class.java)
            intent.putExtra("COMMAND", Command.START.ordinal)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            LocalVpnService.start(context)
        }
    }

    private fun stopServiceInternal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(context, AdVpnService::class.java)
            intent.putExtra("COMMAND", Command.STOP.ordinal)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            LocalVpnService.stop(context)
        }

    }
}
