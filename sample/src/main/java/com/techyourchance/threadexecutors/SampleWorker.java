package com.techyourchance.threadexecutors;

import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import com.techyourchance.threadexecutor.UiThreadExecutor;
import com.techyourchance.threadposter.BackgroundThreadExecutor;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SampleWorker {


    public interface SampleWorkerListener {
        void onWorkDone();
    }

    private final BackgroundThreadExecutor mBackgroundThreadExecutor;
    private final UiThreadExecutor mUiThreadExecutor;

    private final Set<SampleWorkerListener> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<SampleWorkerListener, Boolean>());

    public SampleWorker(BackgroundThreadExecutor backgroundThreadExecutor, UiThreadExecutor uiThreadExecutor) {
        mBackgroundThreadExecutor = backgroundThreadExecutor;
        mUiThreadExecutor = uiThreadExecutor;
    }

    public void registerListener(SampleWorkerListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(SampleWorkerListener listener) {
        mListeners.remove(listener);
    }

    public void doWork() {

        // offload work to background thread
        mBackgroundThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                doWorkSync();
            }
        });
    }

    @WorkerThread
    private void doWorkSync() {

        // simulate 5 seconds worth of work
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // notify listeners on UI thread
        mUiThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                notifyListenersWorkDone();
            }
        });
    }

    @UiThread
    private void notifyListenersWorkDone() {
        for (SampleWorkerListener listener : mListeners) {
            listener.onWorkDone();
        }
    }


}
