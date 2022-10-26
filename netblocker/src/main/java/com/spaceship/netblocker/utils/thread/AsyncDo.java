package com.spaceship.netblocker.utils.thread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


public final class AsyncDo {

    
    public static void call(@NonNull Runnable runnable) {
        call((Callable<Void>) () -> {
            runnable.run();
            return null;
        }, null);
    }

    
    public static <T> void call(@NonNull Callable<T> callable, @Nullable AsyncDoCallback<T> successCallback) {
        call(callable, successCallback, null);
    }

    
    public static <T> void call(@NonNull Callable<T> callable, @Nullable AsyncDoCallback<T> successCallback,
                                @Nullable AsyncDoCallback<Throwable> errorCallback) {
        Task.call(callable, Task.BACKGROUND_EXECUTOR).continueWith((Continuation<T, Void>) task -> {
            if (task.isFaulted()) {
                if (errorCallback != null) {
                    errorCallback.call(task.getError());
                } else {
                    throw task.getError();
                }
            } else if (!task.isCancelled()) {
                if (successCallback != null) {
                    successCallback.call(task.getResult());
                }
            }
            return null;
        }, Task.UI_THREAD_EXECUTOR);
    }

    
    public interface AsyncDoCallback<TTaskResult> {
        
        void call(TTaskResult taskResult);
    }
}
