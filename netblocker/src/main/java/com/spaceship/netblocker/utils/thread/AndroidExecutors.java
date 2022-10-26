package com.spaceship.netblocker.utils.thread;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


 final class AndroidExecutors {

    private static final AndroidExecutors INSTANCE = new AndroidExecutors();

    private final Executor uiThread;

    private AndroidExecutors() {
        uiThread = new UIThreadExecutor();
    }

    
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
     static final int CORE_POOL_SIZE = CPU_COUNT + 1;
     static final int MAX_POOL_SIZE = CPU_COUNT * 2 + 1;
     static final long KEEP_ALIVE_TIME = 1L;

    
    public static ExecutorService newCachedThreadPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());

        allowCoreThreadTimeout(executor, true);

        return executor;
    }

    
    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                threadFactory);

        allowCoreThreadTimeout(executor, true);

        return executor;
    }

    
    @SuppressLint("NewApi")
    public static void allowCoreThreadTimeout(ThreadPoolExecutor executor, boolean value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            executor.allowCoreThreadTimeOut(value);
        }
    }

    
    public static Executor uiThread() {
        return INSTANCE.uiThread;
    }

    
    private static class UIThreadExecutor implements Executor {
        @Override
        public void execute(Runnable command) {
            new Handler(Looper.getMainLooper()).post(command);
        }
    }
}

