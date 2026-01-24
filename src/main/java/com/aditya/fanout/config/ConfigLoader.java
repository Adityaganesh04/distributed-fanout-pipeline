package com.aditya.fanout.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class ConfigLoader {

    public static AppConfig load() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = ConfigLoader.class
                .getClassLoader()
                .getResourceAsStream("application.json");

        if (is == null) {
            throw new RuntimeException("application.json not found");
        }

        return mapper.readValue(is, AppConfig.class);
    }
}
