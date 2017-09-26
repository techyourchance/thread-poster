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
        void onWorkDone(String data);
    }

    private final SampleDataRetriever mSampleDataRetriever;
    private final BackgroundThreadExecutor mBackgroundThreadExecutor;
    private final UiThreadExecutor mUiThreadExecutor;

    private final Set<SampleWorkerListener> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<SampleWorkerListener, Boolean>());



    public SampleWorker(SampleDataRetriever sampleDataRetriever,
                        BackgroundThreadExecutor backgroundThreadExecutor,
                        UiThreadExecutor uiThreadExecutor) {
        mSampleDataRetriever = sampleDataRetriever;
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

        final String data = mSampleDataRetriever.getData();

        // notify listeners on UI thread
        mUiThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                notifyListenersWorkDone(data);
            }
        });
    }

    @UiThread
    private void notifyListenersWorkDone(String data) {
        for (SampleWorkerListener listener : mListeners) {
            listener.onWorkDone(data);
        }
    }


}
