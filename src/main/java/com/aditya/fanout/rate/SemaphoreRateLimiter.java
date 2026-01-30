package com.aditya.fanout.rate;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreRateLimiter {

    private final Semaphore semaphore;

    public SemaphoreRateLimiter(int permitsPerSecond) {
        this.semaphore = new Semaphore(permitsPerSecond);

        Thread refillThread = new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                    semaphore.release(permitsPerSecond - semaphore.availablePermits());
                } catch (Exception ignored) {}
            }
        });
        refillThread.setDaemon(true);
        refillThread.start();
    }

    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }
}
