package com.aditya.fanout.retry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryExecutorTest {

    @Test
    void succeedsBeforeMaxRetries() {
        RetryExecutor retry = new RetryExecutor(3);

        int[] attempts = {0};

        boolean result = retry.execute(() -> {
            attempts[0]++;
            return attempts[0] >= 2;
        });

        assertTrue(result);
        assertEquals(2, attempts[0]);
    }

    @Test
    void failsAfterMaxRetries() {
        RetryExecutor retry = new RetryExecutor(3);

        boolean result = retry.execute(() -> false);

        assertFalse(result);
    }
}
