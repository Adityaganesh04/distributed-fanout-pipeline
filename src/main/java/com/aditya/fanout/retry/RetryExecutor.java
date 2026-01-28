package com.aditya.fanout.retry;

import java.util.concurrent.Callable;

public class RetryExecutor {

    private final int maxRetries;

    public RetryExecutor(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean execute(Callable<Boolean> task) {

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (task.call()) {
                    return true; 
                }
            } catch (Exception ignored) {

            }
        }

        return false;
    }
}
