package com.techyourchance.threadposter.testdoubles;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.Timeout;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class UiThreadPosterTestDoubleTest {

    private static final int TEST_DELAY_MS = 10;

    @ClassRule
    public final static Timeout TIMEOUT = Timeout.seconds(5);

    private UiThreadPosterTestDouble SUT;

    @Before
    public void setup() throws Exception {
        SUT = new UiThreadPosterTestDouble();
    }

    @Test
    public void executeThenJoin_singleRunnable_sideEffectNotVisibleBeforeJoin() throws Exception {
        // Arrange
        final Appender appender = new Appender();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                appender.append("a");
            }
        };
        // Act
        SUT.post(runnable);
        // Assert
        Thread.sleep(TEST_DELAY_MS);
        assertThat(appender.getString(), is(""));
    }


    @Test
    public void executeThenJoin_singleRunnable_sideEffectsVisibleAfterJoin() throws Exception {
        // Arrange
        final Appender appender = new Appender();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                appender.append("a");
            }
        };
        // Act
        SUT.post(runnable);
        // Assert
        SUT.join();
        assertThat(appender.getString(), is("a"));
    }


    @Test
    public void executeThenJoin_multipleRunnables_sideEffectsNotVisibleBeforeJoin() throws Exception {
        // Arrange
        final Appender appender = new Appender();
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                appender.append("a");
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                appender.append("b");
            }
        };
        Runnable runnable3 = new Runnable() {
            @Override
            public void run() {
                appender.append("c");
            }
        };
        // Act
        SUT.post(runnable1);
        SUT.post(runnable2);
        SUT.post(runnable3);
        // Assert
        Thread.sleep(TEST_DELAY_MS);
        assertThat(appender.getString(), is(""));
    }

    @Test
    public void executeThenJoin_multipleRunnables_sideEffectsVisibleAfterJoinInOrder() throws Exception {
        // Arrange
        final Appender appender = new Appender();
        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                appender.append("a");
            }
        };
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                appender.append("b");
            }
        };
        Runnable runnable3 = new Runnable() {
            @Override
            public void run() {
                appender.append("c");
            }
        };
        // Act
        SUT.post(runnable1);
        SUT.post(runnable2);
        SUT.post(runnable3);
        // Assert
        SUT.join();
        assertThat(appender.getString(), is("abc"));
    }


    @Test
    public void executeThenJoin_multipleNestedRunnables_sideEffectsVisibleAfterJoinInReversedOrder() throws Exception {
        // Arrange
        final Appender appender = new Appender();
        final Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                appender.append("a");
            }
        };
        final Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                SUT.post(runnable1);
                appender.append("b");
            }
        };
        final Runnable runnable3 = new Runnable() {
            @Override
            public void run() {
                SUT.post(runnable2);
                appender.append("c");
            }
        };
        // Act
        SUT.post(runnable3);
        // Assert
        SUT.join();
        assertThat(appender.getString(), is("cba"));
    }

    /**
     * This class will be used in order to check side effects in tests
     */
    private class Appender {

        private String mString = "";

        private synchronized void append(String string) {
            mString += string;
        }

        private synchronized String getString() {
            return mString;
        }
    }
}