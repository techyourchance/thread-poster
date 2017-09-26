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

public class SampleWorkerTest {

    private static final String TEST_DATA = "TEST_DATA";

    private ThreadPostersTestController mThreadPostersTestController = new ThreadPostersTestController();

    private SampleDataRetriever mSampleDataRetrieverMock;
    private SampleWorker.SampleWorkerListener mListener1;
    private SampleWorker.SampleWorkerListener mListener2;

    private SampleWorker SUT;

    @Before
    public void setup() throws Exception {
        mSampleDataRetrieverMock = mock(SampleDataRetriever.class);

        when(mSampleDataRetrieverMock.getData()).thenReturn(TEST_DATA);

        SUT = new SampleWorker(
                mSampleDataRetrieverMock,
                mThreadPostersTestController.getBackgroundThreadPosterTestDouble(),
                mThreadPostersTestController.getUiThreadPosterTestDouble());

        mListener1 = mock(SampleWorker.SampleWorkerListener.class);
        mListener2 = mock(SampleWorker.SampleWorkerListener.class);
    }

    @Test
    public void doWork_noListeners_completesWithoutErrors() throws Exception {
        // Arrange
        // Act
        SUT.doWork();
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
        SUT.doWork();
        // Assert

        // needs to be called before assertions in order for all threads to complete and
        // all side effects to be present
        mThreadPostersTestController.join();

        verify(mListener1, times(1)).onWorkDone(ac.capture());
        verify(mListener2, times(1)).onWorkDone(ac.capture());
        List<String> dataList = ac.getAllValues();
        assertThat(dataList.get(0), is(TEST_DATA));
        assertThat(dataList.get(1), is(TEST_DATA));
    }
}