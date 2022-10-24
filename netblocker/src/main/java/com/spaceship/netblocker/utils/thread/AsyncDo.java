package com.spaceship.netblocker.utils.thread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * 对 {@link Task} 的一个简单封装，用于简单的异步编程，回调都发生在 UI 线程上
 * <p>
 * 这个类中提供的方法都是基于当前对 RxJava 的使用的事实上抽离的
 * 如有其他需求，如 delay、任务序列，或者并行执行等，可以直接使用 {@link Task}
 *
 * @author gaoxu
 */
public final class AsyncDo {

    /**
     * 不需要返回值，不关心成功或者失败
     */
    public static void call(@NonNull Runnable runnable) {
        call((Callable<Void>) () -> {
            runnable.run();
            return null;
        }, null);
    }

    /**
     * 只关心成功情况，successCallback 接收到的参数为 callable 的返回值
     */
    public static <T> void call(@NonNull Callable<T> callable, @Nullable AsyncDoCallback<T> successCallback) {
        call(callable, successCallback, null);
    }

    /**
     * 既关心成功也关心失败
     */
    public static <T> void call(@NonNull Callable<T> callable, @Nullable AsyncDoCallback<T> successCallback,
                                @Nullable AsyncDoCallback<Throwable> errorCallback) {
        Task.call(callable, Task.BACKGROUND_EXECUTOR).continueWith((Continuation<T, Void>) task -> {
            if (task.isFaulted()) {
                if (errorCallback != null) {
                    errorCallback.call(task.getError());
                } else {
                    // TODO 暂时抛出异常，暴露问题
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

    /**
     * 对成功和失败情况的一个简单回调，接收参数但没有返回值
     */
    public interface AsyncDoCallback<TTaskResult> {
        /**
         * 执行回调
         *
         * @param taskResult 任务的返回值，或者抛出的异常
         */
        void call(TTaskResult taskResult);
    }
}
