package com.techyourchance.threadposters;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FetchDataUseCaseTest {

    private static final String TEST_DATA = "TEST_DATA";

    private ThreadPostersTestController mThreadPostersTestController = new ThreadPostersTestController();

    private FakeDataRetriever mFakeDataRetrieverMock;
    private FetchDataUseCase.Listener mListener1;
    private FetchDataUseCase.Listener mListener2;

    private FetchDataUseCase SUT;

    @Before
    public void setup() throws Exception {
        mFakeDataRetrieverMock = mock(FakeDataRetriever.class);

        when(mFakeDataRetrieverMock.getData()).thenReturn(TEST_DATA);

        SUT = new FetchDataUseCase(
                mFakeDataRetrieverMock,
                mThreadPostersTestController.getBackgroundThreadPosterTestDouble(),
                mThreadPostersTestController.getUiThreadPosterTestDouble());

        mListener1 = mock(FetchDataUseCase.Listener.class);
        mListener2 = mock(FetchDataUseCase.Listener.class);
    }

    @Test
    public void doWork_noListeners_completesWithoutErrors() throws Exception {
        // Arrange
        // Act
        SUT.fetchData();
        // Assert
        assertThat(true, is(true));
    }

    @Test
    public void doWork_multipleListeners_notifiedWithCorrectData() throws Exception {
        // Arrange
        SUT.registerListener(mListener1);
        SUT.registerListener(mListener2);
        ArgumentCaptor<String> ac = ArgumentCaptor.forClass(String.class);
        // Act
        SUT.fetchData();
        // Assert

        // needs to be called before assertions in order for all threads to complete and
        // all side effects to be present
        mThreadPostersTestController.join();

        verify(mListener1, times(1)).onDataFetched(ac.capture());
        verify(mListener2, times(1)).onDataFetched(ac.capture());
        List<String> dataList = ac.getAllValues();
        assertThat(dataList.get(0), is(TEST_DATA));
        assertThat(dataList.get(1), is(TEST_DATA));
    }
}