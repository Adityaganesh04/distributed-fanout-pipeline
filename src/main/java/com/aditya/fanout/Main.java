package com.aditya.fanout;

import com.aditya.fanout.config.AppConfig;
import com.aditya.fanout.config.ConfigLoader;
import com.aditya.fanout.ingestion.FileIngestor;
import com.aditya.fanout.metrics.MetricsReporter;
import com.aditya.fanout.orchestrator.FanOutOrchestrator;
import com.aditya.fanout.rate.SemaphoreRateLimiter;
import com.aditya.fanout.sink.*;
import com.aditya.fanout.transform.*;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    public static void main(String[] args) throws Exception {

        AppConfig config = ConfigLoader.load();

        BlockingQueue<String> queue =
                new ArrayBlockingQueue<>(config.queueCapacity);

        MetricsReporter metrics = new MetricsReporter();

        Sink rest = new RestSink();
        Sink grpc = new GrpcSink();
        Sink queueSink = new QueueSink();
        Sink wide = new WideColumnSink();

        FanOutOrchestrator orchestrator = new FanOutOrchestrator(
                queue,
                Map.of(
                        rest, new JsonTransformer(),
                        grpc, new ProtoTransformer(),
                        queueSink, new XmlTransformer(),
                        wide, new AvroTransformer()
                ),
                Map.of(
                        rest, new SemaphoreRateLimiter(config.sinks.get("REST").rateLimit),
                        grpc, new SemaphoreRateLimiter(config.sinks.get("GRPC").rateLimit),
                        queueSink, new SemaphoreRateLimiter(config.sinks.get("QUEUE").rateLimit),
                        wide, new SemaphoreRateLimiter(config.sinks.get("WIDE_COLUMN_DB").rateLimit)
                ),
                metrics
        );

        orchestrator.start();

        new FileIngestor().ingest(
                Path.of(config.inputFile),
                queue
        );

        Thread.sleep(2000);
        metrics.stop();
    }
}
