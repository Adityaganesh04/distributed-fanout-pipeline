package com.aditya.fanout.dlq;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DeadLetterQueue {

    private final BlockingQueue<String> dlq = new LinkedBlockingQueue<>();

    public void publish(String record) {
        dlq.offer(record);
    }

    public int size() {
        return dlq.size();
    }
}
