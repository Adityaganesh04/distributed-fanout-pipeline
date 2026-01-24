package com.aditya.fanout.sink;

public class RestSink implements Sink {

    @Override
    public String name() {
        return "REST";
    }

    @Override
    public void send(byte[] payload) {
        // simulate HTTP call
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}
    }
}
