package com.spaceship.netblocker.utils.extensions

/**
 * @author John
 * @since 2019-06-09 21:40
 */

fun <K, V> Map<K, V>?.isNullOrEmpty(): Boolean {
    return this == null || isEmpty()
}

fun <K, V> HashMap<K, V>?.isNullOrEmpty(): Boolean {
    return this == null || isEmpty()
}