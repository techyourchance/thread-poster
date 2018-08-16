# ThreadPoster

Lightweight library for unit testable and expressive multi-threading in Android.

**ThreadPoster is in public beta**. I'm actively looking for feedback on project's structure, naming and usability. Bug reports are also welcome, of course.

## Install

To use ThreadPoster in your project, add this line to your Gradle dependencies configuration:

```
implementation 'com.techyourchance.threadposter:threadposter:0.8.0'
```

## Usage
At the core of this library are two simple classes: `UiThreadPoster` and `BackgroundThreadPoster`.

Code style note: if you're concerned with "boilerplate", you can replace explicit `Runnables` in the examples below with lambdas.

### Executing code on UI thread
In the following example, `updateText(String)` can be safely called on any thread. The actual UI update will always take place on UI thread:

```
public class TextUpdater {

    private final UiThreadPoster mUiThreadPoster;
    private final TextView mTxtSample;
    
    public void updateText(final String text) {
        mUiThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                mTxtSample.setText(text);
            }
        });
    }
}
```

### Executing code on "background" thread
In the following example, `fetchAndCacheUserDetails(String)` can be safely called on any thread. The actual network request and data caching will always take place on background thread (non-UI thread):

```
public class UpdateUserDetailsUseCase {

    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UserDetailsEndpoint mUserDetailsEndpoint;
    private final UserDetailsCache mUserDetailsCache;

    public void fetchAndCacheUserDetails(final String userId) {
        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                UserDetails userDetails = mUserDetailsEndpoint.fetchUserDetails(userId);
                mUserDetailsCache.cacheUserDetails(userDetails);
            }
        });
    }
}
```

### Executing code on "background" thread and notifying Observers on UI thread
In the [following example](sample/src/main/java/com/techyourchance/threadposters/FetchDataUseCase.java), `fetchData()` can be safely called on any thread. The actual data fetch will always take place on background thread, and the observers will always be notified on UI thread:

```
public class FetchDataUseCase {

    public interface Listener {
        void onDataFetched(String data);
        void onDataFetchFailed();
    }

    private final FakeDataFetcher mFakeDataFetcher;
    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UiThreadPoster mUiThreadPoster;

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
```

## License

This project is licensed under the Apache-2.0 License - see the [LICENSE.txt](LICENSE.txt) file for details

