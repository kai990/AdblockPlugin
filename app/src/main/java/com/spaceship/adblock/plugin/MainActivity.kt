package com.spaceship.adblock.plugin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.spaceship.netblocker.utils.loge

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loge("xxx","MainActivity")
    }
}