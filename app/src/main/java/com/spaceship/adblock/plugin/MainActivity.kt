package com.spaceship.adblock.plugin

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.spaceship.netblocker.utils.extensions.safeRun
import com.spaceship.netblocker.utils.loge

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loge("xxx", "MainActivity")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        findViewById<View>(R.id.open_button).setOnClickListener {
            openAdblock()
            finish()
        }
    }
}

private fun MainActivity.openAdblock() {
    val intent = packageManager.getLaunchIntentForPackage("com.spaceship.netprotect12")
    if (intent == null) {
        openInMarket()
    } else {
        startActivity(intent)
    }
}

private fun MainActivity.openInMarket() {
    val pkg = "com.spaceship.netprotect"
    fun getCountryCode(): String = resources.configuration.locale.country
    if (getCountryCode().equals("CN", true)) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
        } catch (e: ActivityNotFoundException) {
            safeRun { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))) }
        }
    } else {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")))
        }
    }
}