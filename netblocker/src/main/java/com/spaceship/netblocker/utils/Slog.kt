package com.spaceship.netblocker.utils

import android.util.Log
import com.spaceship.netblocker.BuildConfig

/**
 * @author John
 * @since 2019-03-24 11:38
 */
object Slog {
    private const val LEVEL_V = 0
    private const val LEVEL_D = 1
    private const val LEVEL_I = 2
    private const val LEVEL_W = 3
    private const val LEVEL_E = 4

    fun v(tag: String? = null, msg: Any?) {
        print(LEVEL_V, tag, msg)
    }

    fun d(tag: String? = null, msg: Any?) {
        print(LEVEL_D, tag, msg)
    }

    fun i(tag: String? = null, msg: Any?) {
        print(LEVEL_I, tag, msg)
    }

    @JvmOverloads
    fun w(tag: String? = null, msg: Any?) {
        print(LEVEL_W, tag, msg)
    }

    fun e(tag: String? = null, msg: Any?) {
        print(LEVEL_E, tag, msg)
    }

    fun e(exception: Throwable?, report: Boolean = false) {
        if (BuildConfig.DEBUG) {
            exception?.printStackTrace()
        }
        if (report) {
        }
    }

    private fun print(level: Int, tag: String?, msg: Any?) {
        if (!BuildConfig.DEBUG or (msg == null)) {
            return
        }
        val t = if (tag.isNullOrEmpty()) "[LOG]" else "[$tag]"
        when (level) {
            LEVEL_V -> Log.v(t, msg.toString())
            LEVEL_D -> Log.d(t, msg.toString())
            LEVEL_I -> Log.i(t, msg.toString())
            LEVEL_W -> Log.w(t, msg.toString())
            LEVEL_E -> Log.e(t, msg.toString())
        }
    }
}
