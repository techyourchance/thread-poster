package com.techyourchance.threadposters;

import android.support.annotation.WorkerThread;

public class SampleDataRetriever {

    @WorkerThread
    public String getData() {

        // simulate 5 seconds worth of work
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "mock data";
    }
}
