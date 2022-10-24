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


/**
 * @author wangkai
 */


fun broadcastReceiver(callback: (Context, Intent) -> Unit): BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = callback(context, intent)
}


/**
 * 从给定的 view 向上寻找 activity 的 context。
 * 使用场景，有时候需要找到 activity 的引用，然而有时候 view 直接 getContent 并不是 Activity ，而是 [android.content.ContextWrapper] 的另外的子类
 */
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
    //如果传入的Context本身就是Activity的Context,那么 getBaseContext 可能拿到 ContextIml 导致返回null
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
