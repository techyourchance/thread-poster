package com.techyourchance.threadposterandroid;

import android.os.Handler;
import android.os.Looper;

/**
 * This class should be used in order to send {@link Runnable} to be executed on UI thread of
 * Android application. If done this way, the logic can remain independent of Android classes
 * even if it explicitly executes code on application's UI thread.
 */
public class UiThreadPoster {

    private final Handler mUiHandler;

    public UiThreadPoster() {
        mUiHandler = getMainHandler();
    }

    /**
     * The only reason this method exists is that {@link UiThreadPosterTestDouble} can override
     * it.
     */
    /* pp */ Handler getMainHandler() {
        return new Handler(Looper.getMainLooper());
    }

    /**
     * Execute {@link Runnable} on application's UI thread.
     * @param runnable {@link Runnable} instance containing the code that should be executed
     */
    public void post(Runnable runnable) {
        mUiHandler.post(runnable);
    }

}
