# ThreadPoster

Lightweight library for unit testable and expressive multi-threading in Android.

**ThreadPoster is in public beta**. I'm actively looking for feedback on project's structure, naming and usability. Bug reports are also welcome, of course.

## Install

To use ThreadPoster in your project, make sure that you have Maven Central set up in your root `build.gradle` script:

```
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

Then add ThreadPoster as a dependency into your main module's (called `app` by default) `build.gradle` script:

```
dependencies {
    implementation 'com.techyourchance:threadposter:1.0.1'
}
```

## Usage
At the core of this library are two simple classes: `UiThreadPoster` and `BackgroundThreadPoster`.

### Executing code on UI thread
In the following example, `updateText(String)` can be safely called on any thread. The actual UI update will always take place on UI thread:

```java
public class TextUpdater {

    private final UiThreadPoster mUiThreadPoster;
    private final TextView mTxtSample;
    
    public void updateText(final String text) {
        mUiThreadPoster.post(() -> {
            mTxtSample.setText(text);
        });
    }
}
```

### Executing code on "background" thread
In the following example, `fetchAndCacheUserDetails(String)` can be safely called on any thread. The actual network request and data caching will always take place on background thread (non-UI thread):

```java
public class UpdateUserDetailsUseCase {

    private final BackgroundThreadPoster mBackgroundThreadPoster;
    private final UserDetailsEndpoint mUserDetailsEndpoint;
    private final UserDetailsCache mUserDetailsCache;

    public void fetchAndCacheUserDetails(final String userId) {
        mBackgroundThreadPoster.post(() -> {
            UserDetails userDetails = mUserDetailsEndpoint.fetchUserDetails(userId);
            mUserDetailsCache.cacheUserDetails(userDetails);
        });
    }
}
```

### Executing code on "background" thread and notifying Observers on UI thread
In the [following example](sample/src/main/java/com/techyourchance/threadposters/FetchDataUseCase.java), `fetchData()` can be safely called on any thread. The actual data fetch will always take place on background thread, and the observers will always be notified on UI thread:

```java
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
        mBackgroundThreadPoster.post(() -> {
            fetchDataSync();
        });
    }

    @WorkerThread
    private void fetchDataSync() {
        try {
            final String data = mFakeDataFetcher.getData();
            mUiThreadPoster.post(() -> {
                notifySuccess(data); // notify listeners on UI thread
            });
        } catch (FakeDataFetcher.DataFetchException e) {
            mUiThreadPoster.post(() -> {
                notifyFailure(); // notify listeners on UI thread
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

## Unit Testing

This library allows for easy unit testing of multithreaded code.

**Important note: no amount of unit testing can guarantee that your code is thread-safe. In other words, even if your unit tests pass, your code can still be subject to race conditions, deadlocks, livelocks, etc.**

To support unit testing, ThreadPosters library is shipped with test double implementations for both `UiThreadPoster` and `BackgroundThreadPoster`. The core feature of these test doubles is that they are truly multi-threaded. In other words, when you unit test using these test doubles, you exercise your code in real multi-threaded environment which is the best approximation of the real production setting.

### Benefits and drawbacks of multi-threaded unit testing

The approach employed by ThreadPoster's test doubles has its benefits and drawbacks.

**Benefits of multi-threaded unit testing:**
1. Exercises the code in real production setting.
2. Has a chance to find multi-threading bugs. This will manifest itself in the form of "flaky" tests (tests that fail ocassionally).

**Drawbacks of multi-threaded unit testing:**
1. Longer unit tests execution times.
2. Requires user assistance in the form of an additional step in each test case (`join()` calls in the examples below).

On my machine, test cases that use ThreadPoster test doubles execute in ~10ms (as opposed to <1ms for plain Java). That's not an issue if you have 100 multi-threaded test cases, but it's a show stopper for proper TDD if you have 1000.

The upside is that absolute majority of your classes shoudln't be multi-threaded, which means that the overall percentage of slow unit tests should be low.

I'm currently working on ways to optimize the test times, but you should definitely keep this drawback in mind if you intend to unit test using ThreadPoster test doubles.

### Example unit test

Below code shows a unit test that makes use of ThreadPoster test doubles. It's part of the sample project.

Note the calls to `mThreadPostersTestDouble.join()` in tests - that's the drawback number two. Since test cases become multithreaded, JUnit can't control tests' execution by itself anymore. 
Therefore, you'll need to call `mThreadPostersTestDouble.join()` before the assertions stage in each of your test cases. This makes sure that all involved threads run to completion and their side effects will be visible during assertions stage.

```java
public class FetchDataUseCaseTest {

    private static final String TEST_DATA = "testData";

    private ThreadPostersTestDouble mThreadPostersTestDouble = new ThreadPostersTestDouble();

    private FakeDataFetcher mFakeDataFetcherMock;
    private FetchDataUseCase.Listener mListener1;
    private FetchDataUseCase.Listener mListener2;

    private FetchDataUseCase SUT;

    @Before
    public void setup() throws Exception {
        mFakeDataFetcherMock = mock(FakeDataFetcher.class);

        SUT = new FetchDataUseCase(
                mFakeDataFetcherMock,
                mThreadPostersTestDouble.getBackgroundTestDouble(),
                mThreadPostersTestDouble.getUiTestDouble());

        mListener1 = mock(FetchDataUseCase.Listener.class);
        mListener2 = mock(FetchDataUseCase.Listener.class);
    }

    @Test
    public void fetchData_successNoListeners_completesWithoutErrors() throws Exception {
        // Arrange
        when(mFakeDataFetcherMock.getData()).thenReturn(TEST_DATA);
        // Act
        SUT.fetchData();
        // Assert

        // needs to be called before assertions in order for all threads to complete and
        // all side effects to be present
        mThreadPostersTestDouble.join();

        assertThat(true, is(true));
    }

    @Test
    public void fetchData_successMultipleListeners_notifiedWithCorrectData() throws Exception {
        // Arrange
        when(mFakeDataFetcherMock.getData()).thenReturn(TEST_DATA);
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        // Act
        SUT.registerListener(mListener1);
        SUT.registerListener(mListener2);
        SUT.fetchData();
        // Assert

        // needs to be called before assertions in order for all threads to complete and
        // all side effects to be present
        mThreadPostersTestDouble.join();

        verify(mListener1).onDataFetched(TEST_DATA);
        verify(mListener2).onDataFetched(TEST_DATA);
    }

    @Test
    public void fetchData_failureMultipleListeners_notifiedOfFailure() throws Exception {
        // Arrange
        doThrow(new FakeDataFetcher.DataFetchException()).when(mFakeDataFetcherMock).getData();
        // Act
        SUT.registerListener(mListener1);
        SUT.registerListener(mListener2);
        SUT.fetchData();
        // Assert

        // needs to be called before assertions in order for all threads to complete and
        // all side effects to be present
        mThreadPostersTestDouble.join();

        verify(mListener1).onDataFetchFailed();
        verify(mListener2).onDataFetchFailed();
    }

}
```

## License

This project is licensed under the Apache-2.0 License - see the [LICENSE.txt](LICENSE.txt) file for details

