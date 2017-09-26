package com.techyourchance.threadposters;

import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import com.techyourchance.threadposterandroid.UiThreadPoster;
import com.techyourchance.threadposter.BackgroundThreadPoster;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SampleWorker {


    public interface SampleWorkerListener {
        void onWorkDone(String data);
    }

    private final SampleDataRetriever mSampleDataRetriever;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UiThreadPoster mUiThreadPoster;

    private final Set<SampleWorkerListener> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<SampleWorkerListener, Boolean>());



    public SampleWorker(SampleDataRetriever sampleDataRetriever,
                        BackgroundThreadPoster backgroundThreadPoster,
                        UiThreadPoster uiThreadPoster) {
        mSampleDataRetriever = sampleDataRetriever;
        mBackgroundThreadPoster = backgroundThreadPoster;
        mUiThreadPoster = uiThreadPoster;
    }

    public void registerListener(SampleWorkerListener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(SampleWorkerListener listener) {
        mListeners.remove(listener);
    }

    public void doWork() {

        // offload work to background thread
        mBackgroundThreadPoster.post(new Runnable() {
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
        mUiThreadPoster.post(new Runnable() {
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
