package com.techyourchance.threadposters;

import com.techyourchance.threadposterandroid.UiThreadPoster;
import com.techyourchance.threadposterandroid.UiThreadPosterTestDouble;
import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.BackgroundThreadPosterTestDouble;

/**
 * This is a convenience wrapper class for tests. It also ensures the correct order of joining
 * of executors.
 */

public class ThreadPostersTestController {

    private  final BackgroundThreadPosterTestDouble mBackgroundThreadPosterTestDouble =
            new BackgroundThreadPosterTestDouble();
    private final UiThreadPosterTestDouble mUiThreadPosterTestDouble =
            new UiThreadPosterTestDouble();

    public void join() {
        // the ordering here is important - usually the work is done in background and then
        // returned to UI thread
        mBackgroundThreadPosterTestDouble.join();
        mUiThreadPosterTestDouble.join();
    }

    public BackgroundThreadPoster getBackgroundThreadPosterTestDouble() {
        return mBackgroundThreadPosterTestDouble;
    }

    public UiThreadPoster getUiThreadPosterTestDouble() {
        return mUiThreadPosterTestDouble;
    }
}
