package com.aditya.fanout.ingestion;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;

public class FileIngestor {

    public void ingest(Path path, BlockingQueue<String> queue) throws Exception {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    queue.put(line);
                }
            }
        }
    }
}
