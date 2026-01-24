package com.aditya.fanout.transform;

public class AvroTransformer implements Transformer {

    @Override
    public byte[] transform(String record) {
        // Simulated Avro / CQL Map payload
        return record.getBytes();
    }
}
