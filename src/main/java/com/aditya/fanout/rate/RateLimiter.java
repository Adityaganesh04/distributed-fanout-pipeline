package com.aditya.fanout.rate;

public interface RateLimiter {
    void acquire() throws InterruptedException;
}
