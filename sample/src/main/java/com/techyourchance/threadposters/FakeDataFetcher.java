package com.techyourchance.threadposters;

import android.support.annotation.WorkerThread;

public class FakeDataFetcher {

    public static class DataFetchException extends Exception {}

    private boolean mIsError = true;

    @WorkerThread
    public String getData() throws DataFetchException {

        // simulate 2 seconds worth of work
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mIsError = !mIsError; // error response every other time

        if (mIsError) {
            throw new DataFetchException();
        } else {
            return "fake data";
        }

    }
}
