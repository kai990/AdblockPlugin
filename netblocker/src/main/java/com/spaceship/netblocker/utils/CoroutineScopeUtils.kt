package com.spaceship.netblocker.utils

import kotlinx.coroutines.*

fun ioScope(unit: suspend () -> Unit) = CoroutineScope(Dispatchers.IO).launch { execute(unit) }

suspend fun contextScope(unit: suspend () -> Unit) = withContext(Dispatchers.Main) { execute(unit) }

fun uiScope(unit: suspend () -> Unit) = CoroutineScope(Dispatchers.Main).launch { execute(unit) }

fun uiDelay(delayMs: Long, unit: suspend () -> Unit) = CoroutineScope(Dispatchers.Main).launch {
    delay(delayMs)
    execute(unit)
}

fun cpuScope(unit: suspend () -> Unit) =
    CoroutineScope(Dispatchers.Default).launch { execute(unit) }


private suspend fun execute(unit: suspend () -> Unit) {
    try {
        unit.invoke()
    } catch (e: Exception) {
        loge(e)
    }
}