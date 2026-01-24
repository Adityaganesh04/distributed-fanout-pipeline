package com.aditya.fanout.config;

import java.util.Map;

public class AppConfig {

    public String inputFile;
    public int queueCapacity;
    public Map<String, SinkConfig> sinks;

    public static class SinkConfig {
        public int rateLimit;
    }
}
