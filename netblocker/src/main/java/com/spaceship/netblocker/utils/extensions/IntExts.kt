package com.spaceship.netblocker.utils.extensions

/**
 * @author John
 * @since 2019-03-02 21:11
 */

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
