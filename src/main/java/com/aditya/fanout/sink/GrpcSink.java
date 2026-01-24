package com.aditya.fanout.sink;

public class GrpcSink implements Sink {

    @Override
    public String name() {
        return "GRPC";
    }

    @Override
    public void send(byte[] payload) {
        try {
            Thread.sleep(5);
        } catch (InterruptedException ignored) {}
    }
}
