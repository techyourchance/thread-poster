# ThreadPoster

Lightweight library for unit testable and expressive multi-threading in Android.

**ThreadPoster is in public beta**. I'm actively looking for feedback on project's structure, naming and usability. Bug reports are also welcome, of course.

## Usage

At the core of this library are two simple classes: `UiThreadPoster` and `BackgroundThreadPoster`.

### Executing code on UI thread
In the following example, `updateText(String)` can be safely called on any thread. The actual UI update will always take place on UI thread:

```
public class TextUpdater {

    private final UiThreadPoster mUiThreadPoster;
    private final TextView mTxtSample;
    
    public TextUpdater(UiThreadPoster uiThreadPoster, TextView txtSample) {
        mUiThreadPoster = uiThreadPoster;
        mTxtSample = txtSample;
    }
    
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
In the following example, `fetchAndCacheUserDetails(String)` can be safely called on any thread. The actual network request and data caching will always take place on "background" thread (as opposed to UI thread):

```
public class UpdateUserDetailsUseCase {

    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UserDetailsEndpoint mUserDetailsEndpoint;
    private final UserDetailsCache mUserDetailsCache;

    public UpdateUserDetailsUseCase(BackgroundThreadPoster backgroundThreadPoster, 
                                    UserDetailsEndpoint userDetailsEndpoint,
                                    UserDetailsCache userDetailsCache) {
        mBackgroundThreadPoster = backgroundThreadPoster;
        mUserDetailsEndpoint = userDetailsEndpoint;
        mUserDetailsCache = userDetailsCache;
    }

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
In the following example, `fetchAndCacheUserDetailsAndNotify(String)` can be safely called on any thread. The actual network request and data caching will always take place on "background" thread, and the observers will always be notified on UI thread:

```
public class UpdateUserDetailsUseCase extends BaseObservable<UpdateUserDetailsUseCase.Listener> {

    public interface Listener {
        void onUserDetailsUpdated(UserDetails userDetails);
        void onUserDetailsUpdateFailed(String userId);
    }

    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UiThreadPoster mUiThreadPoster;
    private final UserDetailsEndpoint mUserDetailsEndpoint;
    private final UserDetailsCache mUserDetailsCache;

    public UpdateUserDetailsUseCase(BackgroundThreadPoster backgroundThreadPoster,
                                    UiThreadPoster uiThreadPoster,
                                    UserDetailsEndpoint userDetailsEndpoint,
                                    UserDetailsCache userDetailsCache) {
        mBackgroundThreadPoster = backgroundThreadPoster;
        mUiThreadPoster = uiThreadPoster;
        mUserDetailsEndpoint = userDetailsEndpoint;
        mUserDetailsCache = userDetailsCache;
    }

    public void fetchAndCacheUserDetailsAndNotify(final String userId) {
        mBackgroundThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                UserDetails userDetails;
                try {
                    userDetails = mUserDetailsEndpoint.fetchUserDetails(userId);
                } catch (NetworkErrorException e) {
                    notifyFailed(userId);
                }
                mUserDetailsCache.cacheUserDetails(userDetails);
                notifySucceeded(userDetails);
            }
        });
    }

    private void notifySucceeded(final UserDetails userDetails) {
        mUiThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                for (Listener listener : getListeners()) {
                    listener.onUserDetailsUpdated(userDetails);
                }
            }
        });
    }

    private void notifyFailed(final String userId) {
        mUiThreadPoster.post(new Runnable() {
            @Override
            public void run() {
                for (Listener listener : getListeners()) {
                    listener.onUserDetailsUpdateFailed(userId);
                }
            }
        });
    }
}
```

## Install

To use ThreadPoster in your project, add this line to your Gradle dependencies configuration:

```
implementation 'com.techyourchance.threadposter:threadposter:0.8.0'
```
## License

This project is licensed under the Apache-2.0 License - see the [LICENSE.txt](LICENSE.txt) file for details

