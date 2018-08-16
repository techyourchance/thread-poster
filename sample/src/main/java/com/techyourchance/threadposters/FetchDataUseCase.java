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
        void onDataFetchFailed();
    }

    private final FakeDataFetcher mFakeDataFetcher;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UiThreadPoster mUiThreadPoster;

    private final Set<Listener> mListeners = Collections.newSetFromMap(
            new ConcurrentHashMap<Listener, Boolean>());

    public FetchDataUseCase(FakeDataFetcher fakeDataFetcher,
                            BackgroundThreadPoster backgroundThreadPoster,
                            UiThreadPoster uiThreadPoster) {
        mFakeDataFetcher = fakeDataFetcher;
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
        try {
            final String data = mFakeDataFetcher.getData();
            mUiThreadPoster.post(new Runnable() { // notify listeners on UI thread
                @Override
                public void run() {
                    notifySuccess(data);
                }
            });
        } catch (FakeDataFetcher.DataFetchException e) {
            mUiThreadPoster.post(new Runnable() { // notify listeners on UI thread
                @Override
                public void run() {
                    notifyFailure();
                }
            });
        }

    }

    @UiThread
    private void notifyFailure() {
        for (Listener listener : mListeners) {
            listener.onDataFetchFailed();
        }
    }

    @UiThread
    private void notifySuccess(String data) {
        for (Listener listener : mListeners) {
            listener.onDataFetched(data);
        }
    }

}
