package com.techyourchance.threadexecutor;

import android.os.Handler;
import android.os.Looper;

/**
 * This class should be used in order to send {@link Runnable} to be executed on UI thread of
 * Android application. If done this way, the logic can remain independent of Android classes
 * even if it explicitly executes code on application's UI thread.
 */
public class UiThreadExecutor {

    private final Handler mUiHandler;

    public UiThreadExecutor() {
        mUiHandler = getMainHandler();
    }

    /**
     * The only reason this method exists is that {@link UiThreadExecutorTestDouble} can override
     * it.
     */
    /* pp */ Handler getMainHandler() {
        return new Handler(Looper.getMainLooper());
    }

    /**
     * Execute {@link Runnable} on application's UI thread. Clients should assume that the execution
     * thread will be totally random.
     * @param runnable {@link Runnable} instance containing the code that should be executed
     */
    public void execute(Runnable runnable) {
        mUiHandler.post(runnable);
    }

}
