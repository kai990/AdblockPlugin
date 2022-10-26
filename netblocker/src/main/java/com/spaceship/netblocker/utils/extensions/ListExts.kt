package com.spaceship.netblocker.utils.extensions




fun <T> List<T>?.addressContains(item: T): Boolean {
    this?.forEach {
        if (it === item) {
            return true
        }
    }
    return false
}