package com.spaceship.netblocker.utils.thread

import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import com.spaceship.netblocker.utils.Slog
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit


object ThreadPool {
    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()

    private val IO_EXECUTOR = AndroidExecutors.newCachedThreadPool()
    private val QUEUE_EXECUTOR = Executors.newSingleThreadExecutor()
    private val UI_EXECUTOR = HandlerExecutor(Handler(Looper.getMainLooper()))
    private val SCHEDULER = Executors.newScheduledThreadPool(CPU_COUNT * 2 + 1)

    fun io(block: () -> Unit) {
        IO_EXECUTOR.execute {
            try {
                block()
            } catch (ex: Exception) {
                Slog.e(ex)
                throw ex
            }
        }
    }

    fun io(runnable: Runnable) {
        IO_EXECUTOR.execute {
            try {
                runnable.run()
            } catch (ex: Exception) {
                Slog.e(ex)
                throw ex
            }
        }
    }

    fun ui(block: () -> Unit) {
        UI_EXECUTOR.execute(block)
    }

    fun uiDelay(delay: Long, block: () -> Unit) {
        UI_EXECUTOR.delay(delay, block)
    }

    fun queue(block: () -> Unit) {
        QUEUE_EXECUTOR.execute(block)
    }

    fun scheduler(delay: Long, block: () -> Unit) {
        SCHEDULER.schedule(block, delay, TimeUnit.MILLISECONDS)
    }

    fun scheduler(block: () -> Unit, delay: Long, unit: TimeUnit) {
        SCHEDULER.schedule(block, delay, unit)
    }

    private class HandlerExecutor(@NonNull handler: Handler) : Executor {
        private val mHandler: Handler = handler

        override fun execute(command: Runnable) {
            if (!mHandler.post(command)) {
                throw RejectedExecutionException("$mHandler is shutting down")
            }
        }

        fun delay(delay: Long, block: () -> Unit) {
            mHandler.postDelayed(block, delay)
        }
    }
}

fun io(block: () -> Unit) {
    ThreadPool.io(block)
}

fun ui(block: () -> Unit) {
    ThreadPool.ui(block)
}
