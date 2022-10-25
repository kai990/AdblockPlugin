package com.spaceship.netblocker.message

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import com.spaceship.netblocker.NetBlocker
import com.spaceship.netblocker.launchEmptyActivity
import com.spaceship.netblocker.utils.logw
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class MessageContentProvider : ContentProvider() {

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = -1

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        values ?: return null
        logw("", "insert uri:$uri,values:$values")
        return Uri.parse("content://finish")
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        TODO("Implement this to handle query requests from clients.")
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        TODO("Implement this to handle requests to update one or more rows.")
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        logw("", "call method:$method,arg:$arg,extras:$extras")
        if(!NetBlocker.isInited()){
            launchEmptyActivity()
            runBlocking { delay(1000) }
        }
        when (method) {
            COMMAND_START_VPN -> NetBlocker.startVpn()
            COMMAND_STOP_VPN -> NetBlocker.stopVpn()
            COMMAND_RESTART_VPN -> NetBlocker.restartVpn()
            COMMAND_IS_VPN_CONNECTED -> return Bundle().apply { putBoolean("isConnected",NetBlocker.isVpnConnected()) }
            COMMAND_UPDATE_BLOCK_APP_LIST->NetBlocker.setAllowedAppList(arg.orEmpty().split(","))
        }
        return null
    }
}