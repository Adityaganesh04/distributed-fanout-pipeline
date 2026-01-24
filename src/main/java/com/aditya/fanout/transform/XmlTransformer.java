package com.aditya.fanout.transform;

public class XmlTransformer implements Transformer {

    @Override
    public byte[] transform(String record) {
        return ("<record>" + record + "</record>").getBytes();
    }
}
