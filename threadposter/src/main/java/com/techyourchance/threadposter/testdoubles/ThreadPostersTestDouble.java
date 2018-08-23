package com.techyourchance.threadposter.testdoubles;

import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

/**
 * This class should be used in unit tests to obtain test doubles of {@link UiThreadPoster} and
 * {@link BackgroundThreadPoster}.<br>
 * The reason for the existence of this class is that it handles special ordering requirements
 * between thread posters when {@link ThreadPostersTestDouble#join()} is called.
 */
public class ThreadPostersTestDouble {

    private final BackgroundThreadPosterTestDouble mBackgroundThreadPosterTestDouble =
            new BackgroundThreadPosterTestDouble();
    private final UiThreadPosterTestDouble mUiThreadPosterTestDouble =
            new UiThreadPosterTestDouble();

    public void join() {
        // The ordering here is important - it's assumed that if both bg and ui thread posters
        // involved, then the flow is from bg thread poster to ui thread poster.
        // If the clients will try to use a reverse order (invoke bg thread poster from ui thread poster),
        // then the tests will simply hang.
        // Not optimal, but, at least, the clients will know that something is wrong right away.
        mBackgroundThreadPosterTestDouble.join();
        mUiThreadPosterTestDouble.join();
    }

    public BackgroundThreadPoster getBackgroundTestDouble() {
        return mBackgroundThreadPosterTestDouble;
    }

    public UiThreadPoster getUiTestDouble() {
        return mUiThreadPosterTestDouble;
    }
}
