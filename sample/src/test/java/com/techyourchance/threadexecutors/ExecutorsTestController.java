package com.techyourchance.threadexecutors;

import com.techyourchance.threadexecutor.UiThreadExecutor;
import com.techyourchance.threadexecutor.UiThreadExecutorTestDouble;
import com.techyourchance.threadposter.BackgroundThreadExecutor;
import com.techyourchance.threadposter.BackgroundThreadExecutorTestDouble;

/**
 * This is a convenience wrapper class for tests. It also ensures the correct order of joining
 * of executors.
 */

public class ExecutorsTestController {

    private  final BackgroundThreadExecutorTestDouble mBackgroundThreadExecutorTestDouble =
            new BackgroundThreadExecutorTestDouble();
    private final UiThreadExecutorTestDouble mUiThreadExecutorTestDouble =
            new UiThreadExecutorTestDouble();

    public void join() {
        // the ordering here is important - usually the work is done in background and then
        // returned to UI thread
        mBackgroundThreadExecutorTestDouble.join();
        mUiThreadExecutorTestDouble.join();
    }

    public BackgroundThreadExecutor getBackgroundThreadExecutorTestDouble() {
        return mBackgroundThreadExecutorTestDouble;
    }

    public UiThreadExecutor getUiThreadExecutorTestDouble() {
        return mUiThreadExecutorTestDouble;
    }
}
