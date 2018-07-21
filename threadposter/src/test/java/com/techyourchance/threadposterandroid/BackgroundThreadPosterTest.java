package com.techyourchance.threadposterandroid;


import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.Semaphore;

public class BackgroundThreadPosterTest {

    private static final int TEST_TIMEOUT_MS = 1000;

    @ClassRule
    public final static Timeout TIMEOUT = Timeout.millis(TEST_TIMEOUT_MS);

    private BackgroundThreadPoster SUT;

    @Before
    public void setup() throws Exception {
        SUT = new BackgroundThreadPoster();
    }

    @Test
    public void execute_singleRunnable_executionSuccessful() throws Exception {
        // Arrange
        final Semaphore semaphore = new Semaphore(0);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                semaphore.release();
            }
        };
        // Act
        SUT.post(runnable);
        // Assert
        semaphore.acquireUninterruptibly();
    }

    @Test
    public void execute_multipleRunnablesIndependent_executionSuccessful() throws Exception {
        // Arrange
        final Semaphore semaphore = new Semaphore(-1);
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                semaphore.release();
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                semaphore.release();
            }
        };
        // Act
        SUT.post(runnable1);
        SUT.post(runnable2);
        // Assert
        semaphore.acquireUninterruptibly();
    }

    @Test
    public void execute_multipleRunnablesInterdependent_executionSuccessful() throws Exception {
        // Arrange
        final Semaphore semaphore1 = new Semaphore(0);
        final Semaphore semaphore2 = new Semaphore(0);
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                semaphore1.acquireUninterruptibly();
                semaphore2.release();
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                semaphore1.release();
            }
        };
        // Act
        SUT.post(runnable1);
        SUT.post(runnable2);
        // Assert
        semaphore2.acquireUninterruptibly();
    }
}