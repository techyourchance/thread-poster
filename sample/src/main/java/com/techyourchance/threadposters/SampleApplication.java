package com.techyourchance.threadposters;

import android.app.Application;

import com.techyourchance.threadposter.BackgroundThreadPoster;
import com.techyourchance.threadposter.UiThreadPoster;

public class SampleApplication extends Application {

    /*
      IMPORTANT:
      Both BackgroundThreadPoster and UiThreadPoster should be global objects (single instance).
     */
    private final BackgroundThreadPoster mBackgroundThreadPoster = new BackgroundThreadPoster();
    private final UiThreadPoster mUiThreadPoster = new UiThreadPoster();

    private final FakeDataFetcher mFakeDataFetcher = new FakeDataFetcher();
    private final FetchDataUseCase mFetchDataUseCase =
            new FetchDataUseCase(mFakeDataFetcher, mBackgroundThreadPoster, mUiThreadPoster);

    public FetchDataUseCase getFetchDataUseCase() {
        return mFetchDataUseCase;
    }
}
