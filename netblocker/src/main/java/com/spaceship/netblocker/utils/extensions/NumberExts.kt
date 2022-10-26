package com.spaceship.netblocker.utils.extensions

import java.text.NumberFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow




fun Number.toHumanByte(): String {
    val unit = 1024.toDouble()
    val bytes = this.toDouble()
    if (bytes < unit) return "$bytes B"
    val exp = (ln(bytes) / ln(unit)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format(Locale.ENGLISH, "%.1f %sB", bytes / unit.pow(exp.toDouble()), pre)
}


fun Number.toHumanK(): String {
    val num = this.toDouble()
    if (num <= 1000) {
        return "${num.toInt()}"
    }
    return NumberFormat.getNumberInstance(Locale.US).format(num / 1000) + "K"
}

fun Float.format(digits: Int) = String.format("%.${digits}f", this)
