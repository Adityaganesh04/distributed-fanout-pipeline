package com.aditya.fanout.orchestrator;

import com.aditya.fanout.metrics.MetricsReporter;
import com.aditya.fanout.rate.SemaphoreRateLimiter;
import com.aditya.fanout.retry.RetryExecutor;
import com.aditya.fanout.dlq.DeadLetterQueue;
import com.aditya.fanout.sink.Sink;
import com.aditya.fanout.transform.Transformer;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FanOutOrchestrator {

    private final BlockingQueue<String> queue;
    private final Map<Sink, Transformer> transformers;
    private final Map<Sink, SemaphoreRateLimiter> limiters;
    private final MetricsReporter metrics;

    private final DeadLetterQueue dlq = new DeadLetterQueue();
    private final RetryExecutor retryExecutor = new RetryExecutor(3);

    public FanOutOrchestrator(
            BlockingQueue<String> queue,
            Map<Sink, Transformer> transformers,
            Map<Sink, SemaphoreRateLimiter> limiters,
            MetricsReporter metrics
    ) {
        this.queue = queue;
        this.transformers = transformers;
        this.limiters = limiters;
        this.metrics = metrics;
    }

    public AutoCloseable start() {

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        Thread metricsThread = new Thread(metrics, "metrics-reporter");
        metricsThread.start();

        for (var entry : transformers.entrySet()) {
            Sink sink = entry.getKey();
            Transformer transformer = entry.getValue();
            SemaphoreRateLimiter limiter = limiters.get(sink);

            executor.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    String record = null;
                    try {
                        record = queue.take(); // backpressure

                        final String payloadSource = record;

                        boolean success = retryExecutor.execute(() -> {
                        try {
                            limiter.acquire();
                            byte[] payload = transformer.transform(payloadSource);
                            sink.send(payload);
                            return true; 
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return false;
                        } catch (Exception e) {
                            return false;
                        }
                    });


                        if (success) {
                            metrics.recordSuccess(sink);
                        } else {
                            dlq.publish(record);
                            metrics.recordFailure(sink);
                            metrics.recordDlq();
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        if (record != null) {
                            dlq.publish(record);
                        }
                        metrics.recordFailure(sink);
                    }
                }
            });
        }

        return () -> {
            metrics.stop();
            metricsThread.interrupt();
            executor.shutdownNow();
        };
    }
}
