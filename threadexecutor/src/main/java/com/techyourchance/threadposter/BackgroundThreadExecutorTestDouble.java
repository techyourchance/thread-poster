package com.techyourchance.threadposter;


import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Test double of {@link BackgroundThreadExecutor} that can be used in tests in order to establish
 * a happens-before relationship between any {@link Runnable} sent to execution and subsequent
 * test assertions.
 */
public class BackgroundThreadExecutorTestDouble extends BackgroundThreadExecutor {

    private final Object MONITOR = new Object();

    private final Queue<Thread> mThreads = new LinkedList<>();

    @Override
    protected ThreadPoolExecutor newThreadPoolExecutor() {
        // in order to support the strategy employed in join() method, we need to ensure that all
        // threads are added to the queue, and are terminated the moment they are idle
        return new ThreadPoolExecutor(
                0,
                Integer.MAX_VALUE,
                0L,
                TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        synchronized (MONITOR) {
                            Thread newThread = new Thread(r);
                            mThreads.add(newThread);
                            return newThread;
                        }
                    }
                }
        );
    }

    /**
     * Call to this method will block until all {@link Runnable}s sent for execution by this
     * "test double" BEFORE THE MOMENT OF A CALL will be completed.<br>
     * Call to this method allows to establish a happens-before relationship between the
     * {@link Runnable}s sent for execution and any subsequent code.
     */
    public void join() {
        Queue<Thread> threadsCopy;
        synchronized (MONITOR) {
            threadsCopy = new LinkedList<>(mThreads);
        }

        Thread thread;
        while ((thread = threadsCopy.poll()) != null) {
            try {

                // the below comment and code don't make sense... however, there probably was a
                // reason why I wrote them, therefore keep them for now (harmless)
                // TODO: review the below code and decide whether it is really needed

                // there is race condition - "self" thread and further threads could be added; we need
                // to wait only until threads before "self" finish
                if (thread.getId() == Thread.currentThread().getId()) {
                    break;
                } else {
                    thread.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
