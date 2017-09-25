package com.techyourchance.threadposter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadPoster {

    private final Executor mExecutorService;

    public ThreadPoster() {
        mExecutorService = Executors.newCachedThreadPool();
    }

    public ThreadPoster(Executor executor) {
        mExecutorService = executor;
    }

    public void post(Runnable runnable) {
        mExecutorService.execute(runnable);
    }
}
