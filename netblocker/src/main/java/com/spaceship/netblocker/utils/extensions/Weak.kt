package com.spaceship.netblocker.utils.extensions

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

/**
 * @version 1.0
 * @since 2018-08-16 21:38
 */
class Weak<T : Any>(initializer: () -> T?) {
    private var weakReference = WeakReference(initializer())

    constructor() : this({
        null
    })

    constructor(value: T?) : this({ value })

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return weakReference.get()
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        weakReference = WeakReference(value)
    }

}