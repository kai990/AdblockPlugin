

package com.spaceship.netblocker

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.spaceship.netblocker.utils.toast

class EmptyActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "VpnRequestActivity"
        private const val REQUEST_CONNECT = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        finish()
    }

    override fun onStop() {
        super.onStop()
        finish()
    }


}

fun launchEmptyActivity() {
    Env.getApp().startActivity(Intent(Env.getApp(), EmptyActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}
