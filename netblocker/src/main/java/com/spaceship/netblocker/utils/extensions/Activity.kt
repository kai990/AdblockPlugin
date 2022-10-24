package com.spaceship.netblocker.utils.extensions

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorInt

/**
 * @author John
 * @since 2019-01-16 21:35
 */

fun Context?.isActivityLive(): Boolean {
    val activity = this as? Activity ?: return false
    return !activity.isDestroyed && activity.window.decorView.isShown
}

fun Activity?.setStatusBarColor(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || this == null) {
        return
    }
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color
    if (color == Color.WHITE) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    } else {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}

fun Activity?.setNavigationBarColor(@ColorInt color: Int) {
    if (this == null) {
        return
    }
    window.navigationBarColor = color
}