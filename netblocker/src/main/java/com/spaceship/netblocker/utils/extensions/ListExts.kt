package com.spaceship.netblocker.utils.extensions

/**
 * @author John
 * @since 2019-09-17 23:34
 */

/**
 * 查找 list 中是否包含 item （地址相同）
 */
fun <T> List<T>?.addressContains(item: T): Boolean {
    this?.forEach {
        if (it === item) {
            return true
        }
    }
    return false
}