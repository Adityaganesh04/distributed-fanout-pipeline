package com.aditya.fanout.sink;

public class WideColumnSink implements Sink {

    @Override
    public String name() {
        return "WIDE_COLUMN_DB";
    }

    @Override
    public void send(byte[] payload) {
        try {
            Thread.sleep(1);
        } catch (InterruptedException ignored) {}
    }
}
