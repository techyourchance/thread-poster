package com.techyourchance.threadposters;

import com.techyourchance.threadposter.testdoubles.ThreadPostersTestDouble;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FetchDataUseCaseTest {

    private static final String TEST_DATA = "testData";

    private ThreadPostersTestDouble mThreadPostersTestDouble;

    private FakeDataFetcher mFakeDataFetcherMock;
    private FetchDataUseCase.Listener mListener1;
    private FetchDataUseCase.Listener mListener2;

    private FetchDataUseCase SUT;

    @Before
    public void setup() throws Exception {
        mFakeDataFetcherMock = mock(FakeDataFetcher.class);
        mThreadPostersTestDouble = new ThreadPostersTestDouble();

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
        success();
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
        success();
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
        failure();
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

    // ---------------------------------------------------------------------------------------------
    // Helper methods
    // ---------------------------------------------------------------------------------------------

    private void success() throws FakeDataFetcher.DataFetchException {
        when(mFakeDataFetcherMock.getData()).thenReturn(TEST_DATA);
    }

    private void failure() throws FakeDataFetcher.DataFetchException {
        doThrow(new FakeDataFetcher.DataFetchException()).when(mFakeDataFetcherMock).getData();
    }
}