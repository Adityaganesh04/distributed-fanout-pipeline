package com.aditya.fanout.sink;

public interface Sink {
    void send(byte[] payload);
    String name();
}
