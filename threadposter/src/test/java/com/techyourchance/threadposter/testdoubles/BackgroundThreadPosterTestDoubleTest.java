package com.techyourchance.threadposter.testdoubles;


import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class BackgroundThreadPosterTestDoubleTest {

    private static final int TEST_DELAY_MS = 10;

    @ClassRule
    public final static Timeout TIMEOUT = Timeout.seconds(5);

    private BackgroundThreadPosterTestDouble SUT;

    @Before
    public void setup() throws Exception {
        SUT = new BackgroundThreadPosterTestDouble();
    }

    @Test
    public void executeThenJoin_singleRunnable_sideEffectsNotVisibleBeforeJoin() throws Exception {
        // Arrange
        final Counter counter = new Counter();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                counter.increment();
            }
        };
        // Act
        SUT.post(runnable);
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
                counter.increment();
            }
        };
        // Act
        SUT.post(runnable);
        // Assert
        SUT.join();
        assertThat(counter.getCount(), is(1));
    }

    @Test
    public void executeThenJoin_multipleRunnablesIndependent_sideEffectsNotVisibleBeforeJoin() throws Exception {
        // Arrange
        final Counter counter = new Counter();
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                counter.increment();
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                counter.increment();
            }
        };
        // Act
        SUT.post(runnable1);
        SUT.post(runnable2);
        // Assert
        Thread.sleep(TEST_DELAY_MS);
        assertThat(counter.getCount(), is(0));
    }

    @Test
    public void executeThenJoin_multipleRunnablesIndependent_sideEffectsVisibleAfterJoin() throws Exception {
        // Arrange
        final Counter counter = new Counter();
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                counter.increment();
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                counter.increment();
            }
        };
        // Act
        SUT.post(runnable1);
        SUT.post(runnable2);
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
                semaphore.acquireUninterruptibly();
                counter.increment();
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                semaphore.release();
                counter.increment();
            }
        };
        // Act
        SUT.post(runnable1);
        SUT.post(runnable2);
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
                semaphore.acquireUninterruptibly();
                counter.increment();
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                semaphore.release();
                counter.increment();
            }
        };
        // Act
        SUT.post(runnable1);
        SUT.post(runnable2);
        // Assert
        SUT.join();
        assertThat(counter.getCount(), is(2));
    }

    @Test
    public void executeThenJoin_multipleNestedRunnablesInterdependent_sideEffectsVisibleAfterJoin() throws Exception {
        // Arrange
        final Counter counter = new Counter();
        final Semaphore semaphore = new Semaphore(0);
        final Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                semaphore.release();
                counter.increment();
            }
        };
        final Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                SUT.post(runnable1);
                semaphore.acquireUninterruptibly();
                counter.increment();
            }
        };
        final Runnable runnable3 = new Runnable() {
            @Override
            public void run() {
                SUT.post(runnable2);
                counter.increment();
            }
        };
        // Act
        SUT.post(runnable3);
        // Assert
        SUT.join();
        assertThat(counter.getCount(), is(3));
    }

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

}