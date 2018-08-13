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

    private final FakeDataRetriever mFakeDataRetriever = new FakeDataRetriever();
    private final FetchDataUseCase mFetchDataUseCase =
            new FetchDataUseCase(mFakeDataRetriever, mBackgroundThreadPoster, mUiThreadPoster);

    public FetchDataUseCase getFetchDataUseCase() {
        return mFetchDataUseCase;
    }
}
