package com.techyourchance.threadposter;

import android.os.Handler;
import android.os.Looper;

public class UiThreadPoster {

    private final Handler mUiHandler;

    public UiThreadPoster() {
        mUiHandler = getMainHandler();
    }

    /**
     * Execute {@link Runnable} on application's UI thread.
     * @param runnable {@link Runnable} instance containing the code that should be executed
     */
    public void post(Runnable runnable) {
        mUiHandler.post(runnable);
    }

    /**
     * The only reason this method exists is that {@link UiThreadPosterTestDouble} can override
     * it.
     */
    protected Handler getMainHandler() {
        return new Handler(Looper.getMainLooper());
    }

}
