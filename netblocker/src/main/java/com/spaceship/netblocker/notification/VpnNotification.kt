package com.spaceship.netblocker.notification

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import com.spaceship.netblocker.R
import com.spaceship.netblocker.VpnForegroundNotification
import com.spaceship.netblocker.utils.RR
import io.karn.notify.BuildConfig
import io.karn.notify.Notify

const val ACTION_NONE = "action_none"

/**
 * @author John
 * @since 2019-06-16 09:43
 */
class VpnNotification(
    private val context: Context
) : VpnForegroundNotification {

    private val notificationManager by lazy {
        context.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun id(): Int = 100

    private var isCanceled = false

    /**
     * 开始连接
     */
    override fun notification(): Notification {
        isCanceled = false
        return createNotification(
            RR.getString(R.string.nty_vpn_starting_title),
            RR.getString(R.string.nty_vpn_starting_content),
            ACTION_NONE
        )
    }

    private fun createNotification(
        title: String,
        content: String,
        action: String = ACTION_NONE
    ): Notification {
        return Notify
            .with(context)
            .alerting(Notify.CHANNEL_DEFAULT_KEY + BuildConfig.APPLICATION_ID) {
                channelName = RR.getString(R.string.block_notification_channel_name)
                channelImportance = Notify.IMPORTANCE_MIN
            }
            .header {
                icon = R.drawable.ic_vpn_btn_on
                color = Color.parseColor("#6f6f6f")
            }
            .content {
                this.title = title
                text = content
            }
            .meta { clickIntent = createIntent(action) }
            .asBuilder().build()
    }

    fun update(title: String, content: String, action: String = ACTION_NONE) {
        notificationManager.notify(id(), createNotification(title, content, action))
    }

    fun update(bundle: Bundle?) {
        bundle ?: return
        update(bundle.getString("title").orEmpty(), bundle.getString("content").orEmpty())
    }

    fun cancel() {
        isCanceled = true
        notificationManager.cancel(id())
    }

    private fun createIntent(action: String): PendingIntent {
        val intent = Intent().apply { component = ComponentName("com.spaceship.netprotect", "com.spaceship.netprotect.page.home.MainActivity") }
        intent.action = action
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, 0, intent, FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(context, 0, intent, 0)
        }
    }
}
