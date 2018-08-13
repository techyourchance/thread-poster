package com.techyourchance.threadposters;

import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;

import com.techyourchance.threadposter.UiThreadPoster;
import com.techyourchance.threadposter.BackgroundThreadPoster;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FetchDataUseCase {

    public interface Listener {
        void onDataFetched(String data);
    }

    private final FakeDataRetriever mFakeDataRetriever;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UiThreadPoster mUiThreadPoster;

    private final Set<Listener> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<Listener, Boolean>());

    public FetchDataUseCase(FakeDataRetriever fakeDataRetriever,
                            BackgroundThreadPoster backgroundThreadPoster,
                            UiThreadPoster uiThreadPoster) {
        mFakeDataRetriever = fakeDataRetriever;
        mBackgroundThreadPoster = backgroundThreadPoster;
        mUiThreadPoster = uiThreadPoster;
    }

    public void registerListener(Listener listener) {
        mListeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        mListeners.remove(listener);
    }

    public void fetchData() {
        // offload work to background thread
        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                fetchDataSync();
            }
        });
    }

    @WorkerThread
    private void fetchDataSync() {
        final String data = mFakeDataRetriever.getData();
        // notify listeners on UI thread
        mUiThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                notifySuccess(data);
            }
        });
    }

    @UiThread
    private void notifySuccess(String data) {
        for (Listener listener : mListeners) {
            listener.onDataFetched(data);
        }
    }

}
