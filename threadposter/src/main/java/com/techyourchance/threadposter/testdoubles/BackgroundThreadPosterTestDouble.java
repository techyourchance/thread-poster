package com.techyourchance.threadposter.testdoubles;


import com.techyourchance.threadposter.BackgroundThreadPoster;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test double of {@link BackgroundThreadPoster} that can be used in tests in order to establish
 * a happens-before relationship between any {@link Runnable} sent to execution and subsequent
 * test assertions.
 */
/* pp */  class BackgroundThreadPosterTestDouble extends BackgroundThreadPoster {

    private final Object MONITOR = new Object();

    private final Queue<Runnable> mRunnables = new ConcurrentLinkedQueue<>();

    private int mNonCompletedRunnables = 0;

    @Override
    public void post(Runnable runnable) {
        synchronized (MONITOR) {
            mRunnables.add(runnable);
            mNonCompletedRunnables++;
            MONITOR.notifyAll();
        }
    }

    @Override
    protected ThreadPoolExecutor newThreadPoolExecutor() {
        return new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                0L,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(final Runnable r) {
                        return new Thread(new Runnable() {
                            @Override
                            public void run() {
                                r.run();
                                synchronized (MONITOR) {
                                    mNonCompletedRunnables--;
                                    MONITOR.notifyAll();
                                }
                            }
                        });
                    }
                }
        );
    }

    /**
     * Execute all {@link Runnable}s posted to this "test double". The caller will block until the operation completes<br>
     * Call to this method allows to establish a happens-before relationship between the previously
     * posted {@link Runnable}s and subsequent code.
     */
    /* pp */ void join() {
        synchronized (MONITOR) {
            Runnable runnable;
            while (mNonCompletedRunnables > 0) {
                while ((runnable = mRunnables.poll()) != null) {
                    super.post(runnable);
                }
                try {
                    MONITOR.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException("interrupted");
                }
            }
        }
    }
}
