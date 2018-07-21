package com.techyourchance.threadposterandroid;

import android.os.Handler;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Test double of {@link UiThreadPoster} that can be used in tests in order to establish
 * a happens-before relationship between any {@link Runnable} sent to execution and subsequent
 * test assertions.
 * Instead of using Android's UI (aka main) thread, this implementation sends each {@link Runnable}
 * to a new background thread. Only one background thread is allowed to run at a time, thus
 * simulating a serial execution of {@link Runnable}s.
 */
public class UiThreadPosterTestDouble extends UiThreadPoster {

    private final Object MONITOR = new Object();

    private final Queue<Thread> mThreads = new LinkedList<>();

    @Override
    protected Handler getMainHandler() {
        // need to override this method in order to prevent "stub" RuntimeException during unit
        // testing; since this class does not use Handler at all, we can simply return null
        return null;
    }

    @Override
    public void post(final Runnable runnable) {
        synchronized (MONITOR) {
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    // make sure all previous threads finished
                    UiThreadPosterTestDouble.this.join();
                    runnable.run();
                }
            });
            mThreads.add(worker);
            worker.start();
        }
    }

    /**
     * Call to this method will block until all {@link Runnable}s posted to this "test double"
     * BEFORE THE MOMENT OF A CALL will be completed.<br>
     * Call to this method allows to establish a happens-before relationship between the previously
     * posted {@link Runnable}s and subsequent code.
     */
    public void join() {
        Queue<Thread> threadsCopy;
        synchronized (MONITOR) {
            threadsCopy = new LinkedList<>(mThreads);
        }

        Thread thread;
        while ((thread = threadsCopy.poll()) != null) {
            try {

                // due to the way post(Runnable) is being implemented, this method will be called
                // by threads that were added to the queue; in this case, we need to join only on
                // threads that precede the calling thread in the queue
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
