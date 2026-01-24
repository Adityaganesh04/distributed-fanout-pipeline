package com.aditya.fanout.transform;

public class ProtoTransformer implements Transformer {

    @Override
    public byte[] transform(String record) {
        // Simulated Protobuf binary
        return record.getBytes();
    }
}
