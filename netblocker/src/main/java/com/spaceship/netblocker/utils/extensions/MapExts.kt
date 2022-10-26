package com.spaceship.netblocker.utils.extensions



fun <K, V> Map<K, V>?.isNullOrEmpty(): Boolean {
    return this == null || isEmpty()
}

fun <K, V> HashMap<K, V>?.isNullOrEmpty(): Boolean {
    return this == null || isEmpty()
}