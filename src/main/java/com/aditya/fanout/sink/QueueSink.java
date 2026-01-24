package com.aditya.fanout.sink;

public class QueueSink implements Sink {

    @Override
    public String name() {
        return "QUEUE";
    }

    @Override
    public void send(byte[] payload) {
        try {
            Thread.sleep(2);
        } catch (InterruptedException ignored) {}
    }
}
