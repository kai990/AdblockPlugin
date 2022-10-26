package com.spaceship.netblocker.message

import android.app.Activity
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.gson.Gson
import com.spaceship.netblocker.Env
import com.spaceship.netblocker.ProxyDispatcher
import com.spaceship.netblocker.model.DispatchPacket
import com.spaceship.netblocker.utils.extensions.safeRun

const val COMMAND_START_VPN = "start_vpn"
const val COMMAND_STOP_VPN = "stop_vpn"
const val COMMAND_RESTART_VPN = "restart_vpn"
const val COMMAND_IS_VPN_CONNECTED = "is_vpn_connected"
const val COMMAND_VPN_STATUS = "vpn_status"
const val COMMAND_DOMAIN_DISPATCH = "domain_dispatch"
const val COMMAND_UPDATE_NOTIFICATION = "update_notification"
const val COMMAND_CANCEL_NOTIFICATION = "cancel_notification"
const val COMMAND_UPDATE_BLOCK_APP_LIST = "update_block_app_list"

const val COMMAND_BOOLEAN_PARAM = "command_boolean_param"
const val COMMAND_INT_PARAM = "command_int_param"
const val COMMAND_STRING_PARAM = "command_string_param"

// call adblock app
private val AUTHORITIES = Uri.parse("content://com.spaceship.netprotect.message.provider")

private fun resolver(): ContentResolver = Env.getApp().contentResolver

private fun call(method: String, arg: String? = null, extras: Bundle? = null): Bundle? {
    return try {
        resolver().call(AUTHORITIES, method, arg, extras)
    } catch (e: Exception) {
        openPluginEmptyActivity()
        null
    }
}

fun dispatchDomain(packet: DispatchPacket): Int {
    val bundle = call(COMMAND_DOMAIN_DISPATCH, arg = Gson().toJson(packet))
    return bundle?.getInt("COMMAND_INT_PARAM") ?: ProxyDispatcher.TYPE_DIRECT
}

fun sendVpnStatus(status: Int) {
    call(COMMAND_VPN_STATUS, arg = "$status")
}

private fun openPluginEmptyActivity() {
    safeRun {
        val context = Env.getApp()
        val intent = Intent()
        intent.component = ComponentName("com.spaceship.netprotect", "com.spaceship.netprotect.page.utils.EmptyActivity")
        intent.putExtra("flag", "flag")
        if (context !is Activity) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}