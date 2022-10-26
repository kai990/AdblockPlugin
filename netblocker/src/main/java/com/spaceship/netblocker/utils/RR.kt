package com.spaceship.netblocker.utils

import android.graphics.Color
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.spaceship.netblocker.Env


object RR {
    fun color(@ColorRes res: Int) = Env.getApp().resources.getColor(res)

    fun getDarkerColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] *= 0.8f
        return Color.HSVToColor(hsv)
    }

    fun stringArray(@ArrayRes res: Int) = Env.getApp().resources.getStringArray(res)

    fun getString(@StringRes res: Int): String = Env.getApp().getString(res)

    fun getString(@StringRes res: Int, vararg array: Any): String = if (array.isNotEmpty()) {
        Env.getApp().getString(res, *array)
    } else {
        Env.getApp().getString(res)
    }

    fun getString(@StringRes res: Int, @StringRes vararg array: Int): String = if (array.isNotEmpty()) {
        val parses = mutableListOf<String>()
        for (i: Int in array) {
            parses.add(RR.getString(i))
        }
        parses.toTypedArray()
        Env.getApp().getString(res, parses.toTypedArray())
    } else {
        Env.getApp().getString(res)
    }
}
