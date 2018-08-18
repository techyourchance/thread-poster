package com.techyourchance.threadposter;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BackgroundThreadPoster {

    private static final int CORE_THREADS = 3;
    private static final long KEEP_ALIVE_SECONDS = 60L;

    private final ThreadPoolExecutor mThreadPoolExecutor;

    public BackgroundThreadPoster() {
        mThreadPoolExecutor = newThreadPoolExecutor();
    }

    /**
     * Execute {@link Runnable} on a random background thread.
     * @param runnable {@link Runnable} instance containing the code that should be executed
     */
    public final void post(Runnable runnable) {
        mThreadPoolExecutor.execute(runnable);
    }

    /**
     * Get the underlying {@link ThreadPoolExecutor}.
     * In general, this method shouldn't be used and is provided only for the purpose of
     * integration with existing libraries and frameworks.
     */
    protected final ThreadPoolExecutor getThreadPoolExecutor() {
        return mThreadPoolExecutor;
    }

    /**
     * Get the underlying {@link ThreadFactory}.
     * In general, this method shouldn't be used and is provided only for the purpose of
     * integration with existing libraries and frameworks.
     */
    protected final ThreadFactory getThreadFactory() {
        return getThreadPoolExecutor().getThreadFactory();
    }

    /**
     * This factory method constructs the instance of {@link ThreadPoolExecutor} that is used by
     * {@link BackgroundThreadPoster} internally.<br>
     * The returned executor has sensible defaults for Android applications.<br>
     * Override only if you're ABSOLUTELY sure that you know what you're doing.
     */
    protected ThreadPoolExecutor newThreadPoolExecutor() {
        return new ThreadPoolExecutor(
                CORE_THREADS,
                Integer.MAX_VALUE,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>()
        );
    }

}
