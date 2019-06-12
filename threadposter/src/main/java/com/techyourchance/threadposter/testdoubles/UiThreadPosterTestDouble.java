package com.techyourchance.threadposter.testdoubles;

import android.os.Handler;

import com.techyourchance.threadposter.UiThreadPoster;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Test double of {@link UiThreadPoster} that can be used in tests in order to establish
 * a happens-before relationship between any {@link Runnable} sent to execution and subsequent
 * test assertions.
 * Instead of using Android's UI (aka main) thread, this implementation runs all {@link Runnable}s
 * on a single background thread in order, thus simulating serial execution on UI thread.
 */
/* pp */  class UiThreadPosterTestDouble extends UiThreadPoster {

    private final Queue<Runnable> mRunnables = new ConcurrentLinkedQueue<>();

    @Override
    protected Handler getMainHandler() {
        // need to override this method in order to prevent "stub" RuntimeException during unit
        // testing; since this class does not use Handler at all, we can simply return null
        return null;
    }

    @Override
    public void post(final Runnable runnable) {
        mRunnables.add(runnable);
    }

    /**
     * Execute all {@link Runnable}s posted to this "test double". The caller will block until the operation completes<br>
     * Call to this method allows to establish a happens-before relationship between the previously
     * posted {@link Runnable}s and subsequent code.
     */
    public void join() {
        final Thread fakeUiThread = new Thread() {
            @Override
            public void run() {
                Runnable runnable;
                while ((runnable = mRunnables.poll()) != null) {
                    runnable.run();
                }
            }
        };

        fakeUiThread.start();

        try {
            fakeUiThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted");
        }
    }


}
