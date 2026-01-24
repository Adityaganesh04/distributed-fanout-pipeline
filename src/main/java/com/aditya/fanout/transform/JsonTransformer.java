package com.aditya.fanout.transform;

public class JsonTransformer implements Transformer {

    @Override
    public byte[] transform(String record) {
        // Mock JSON transform
        return ("{\"data\":\"" + record + "\"}").getBytes();
    }
}
