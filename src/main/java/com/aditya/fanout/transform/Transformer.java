package com.aditya.fanout.transform;

public interface Transformer {
    byte[] transform(String record) throws Exception;
}
