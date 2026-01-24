package com.aditya.fanout.transform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonTransformerTest {

    @Test
    void shouldTransformStringToJsonBytes() throws Exception {

        JsonTransformer transformer = new JsonTransformer();
        String input = "test-record";

        byte[] output = transformer.transform(input);

        assertNotNull(output);
        String result = new String(output);

        assertTrue(result.contains("test-record"));
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
    }
}
