package com.spaceship.netblocker.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.view.View
import com.spaceship.netblocker.Env





fun broadcastReceiver(callback: (Context, Intent) -> Unit): BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = callback(context, intent)
}



fun findActivity(view: View?): Activity? {
    return if (view == null) {
        null
    } else getActivityFromContext(view.context)
}

fun isActivityAlive(view: View?): Boolean {
    if (view == null) return false
    val activity = getActivityFromContext(view.context)
    return isActivityAlive(activity)
}

fun isActivityAlive(context: Context?): Boolean {
    if (context == null) return false
    val activity = getActivityFromContext(context)
    return isActivityAlive(activity)
}

fun isActivityAlive(activity: Activity?): Boolean {
    return !(activity == null || activity.isFinishing || activity.isDestroyed)
}

fun getActivityFromContext(context: Context): Activity? {
    var context = context
    if (context is Activity) {
        return context
    }
    if (context is ContextWrapper) {
        context = context.baseContext
    }
    return if (context is Activity) {
        context
    } else null
}

fun isServiceActive(clz: Class<out Service>): Boolean {
    val manager = Env.getApp().getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
    manager.getRunningServices(Integer.MAX_VALUE)?.forEach {
        if (clz.name == it.service.className) {
            return true
        }
    }
    return false
}
