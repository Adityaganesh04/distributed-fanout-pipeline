package com.aditya.fanout.sink;

public class RestSink implements Sink {

    @Override
    public String name() {
        return "REST";
    }

    @Override
    public void send(byte[] payload) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}
    }
}
