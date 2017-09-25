package com.techyourchance.threadposter;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BackgroundThreadExecutorTestDoubleTest {

    private static final int TEST_TIMEOUT_MS = 1000;
    private static final int TEST_DELAY_MS = TEST_TIMEOUT_MS / 10;

    @ClassRule
    public final static Timeout TIMEOUT = Timeout.millis(TEST_TIMEOUT_MS);

    /**
     * This class will be used in order to check side effects in tests
     */
    private class Counter {

        private AtomicInteger mCount = new AtomicInteger(0);

        private void increment() {
            mCount.incrementAndGet();
        }

        private int getCount() {
            return mCount.get();
        }
    }

    private BackgroundThreadExecutorTestDouble SUT;

    @Before
    public void setup() throws Exception {
        SUT = new BackgroundThreadExecutorTestDouble();
    }

    @Test
    public void executeThenJoin_singleRunnable_sideEffectsNotVisibleBeforeJoin() throws Exception {
        // Arrange
        final Counter counter = new Counter();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2 * TEST_DELAY_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter.increment();
            }
        };
        // Act
        SUT.execute(runnable);
        // Assert
        Thread.sleep(TEST_DELAY_MS);
        assertThat(counter.getCount(), is(0));
    }

    @Test
    public void executeThenJoin_singleRunnable_sideEffectsVisibleAfterJoin() throws Exception {
        // Arrange
        final Counter counter = new Counter();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TEST_DELAY_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter.increment();
            }
        };
        // Act
        SUT.execute(runnable);
        // Assert
        SUT.join();
        assertThat(counter.getCount(), is(1));
    }

    @Test
    public void executeThenJoin_multipleRunnablesIndependent_sideEffectsNotVisibleBeforeJoin() throws Exception {
        // Arrange
        final Counter counter = new Counter();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2 * TEST_DELAY_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter.increment();
            }
        };
        // Act
        SUT.execute(runnable);
        SUT.execute(runnable);
        // Assert
        Thread.sleep(TEST_DELAY_MS);
        assertThat(counter.getCount(), is(0));
    }

    @Test
    public void executeThenJoin_multipleRunnablesIndependent_sideEffectsVisibleAfterJoin() throws Exception {
        // Arrange
        final Counter counter = new Counter();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TEST_DELAY_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter.increment();
            }
        };
        // Act
        SUT.execute(runnable);
        SUT.execute(runnable);
        // Assert
        SUT.join();
        assertThat(counter.getCount(), is(2));
    }

    @Test
    public void executeThenJoin_multipleRunnablesInterdependent_sideEffectsNotVisibleBeforeJoin() throws Exception {
        // Arrange
        final Counter counter = new Counter();
        final Semaphore semaphore = new Semaphore(0);
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2 * TEST_DELAY_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                semaphore.release();
                counter.increment();
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                semaphore.acquireUninterruptibly();
                counter.increment();
            }
        };
        // Act
        SUT.execute(runnable1);
        SUT.execute(runnable2);
        // Assert
        Thread.sleep(TEST_DELAY_MS);
        assertThat(counter.getCount(), is(0));
    }

    @Test
    public void executeThenJoin_multipleRunnablesInterdependent_sideEffectsVisibleAfterJoin() throws Exception {
        // Arrange
        final Counter counter = new Counter();
        final Semaphore semaphore = new Semaphore(0);
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TEST_DELAY_MS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                semaphore.release();
                counter.increment();
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                semaphore.acquireUninterruptibly();
                counter.increment();
            }
        };
        // Act
        SUT.execute(runnable1);
        SUT.execute(runnable2);
        // Assert
        SUT.join();
        assertThat(counter.getCount(), is(2));
    }
}