package com.spaceship.netblocker.utils.extensions

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty


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