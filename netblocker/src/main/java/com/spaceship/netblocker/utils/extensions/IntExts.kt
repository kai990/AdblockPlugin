package com.spaceship.netblocker.utils.extensions



fun String?.toIntSafty(): Int {
    this?.let {
        return try {
            Integer.parseInt(it)
        } catch (e: Exception) {
            0
        }
    }
    return 0
}

fun String?.toLongSafty(): Long {
    this?.let {
        return try {
            this.toLong()
        } catch (e: Exception) {
            0
        }
    }
    return 0
}
