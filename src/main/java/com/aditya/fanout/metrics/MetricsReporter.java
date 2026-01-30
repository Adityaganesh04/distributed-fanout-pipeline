package com.aditya.fanout.metrics;

import com.aditya.fanout.sink.Sink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class MetricsReporter implements Runnable {

    private final Map<String, AtomicLong> success = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> failure = new ConcurrentHashMap<>();

    private final AtomicLong totalProcessed = new AtomicLong();
    private final AtomicLong dlqCount = new AtomicLong();

    private final AtomicBoolean running = new AtomicBoolean(true);

    private long lastPrintedTotal = 0;

    public void recordSuccess(Sink sink) {
        success.computeIfAbsent(sink.name(), k -> new AtomicLong()).incrementAndGet();
        totalProcessed.incrementAndGet();
    }

    public void recordFailure(Sink sink) {
        failure.computeIfAbsent(sink.name(), k -> new AtomicLong()).incrementAndGet();
        totalProcessed.incrementAndGet();
    }

    public void recordDlq() {
        dlqCount.incrementAndGet();
    }

    public void stop() {
        running.set(false);
    }

    @Override
    public void run() {
        while (running.get()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            long currentTotal = totalProcessed.get();
            long processedInWindow = currentTotal - lastPrintedTotal;
            double throughput = processedInWindow / 5.0;
            lastPrintedTotal = currentTotal;

            System.out.println("=== METRICS ===");

            for (String sink : success.keySet()) {
                long s = success.getOrDefault(sink, new AtomicLong(0)).get();
                long f = failure.getOrDefault(sink, new AtomicLong(0)).get();
                System.out.printf("%s | success=%d | failure=%d%n", sink, s, f);
            }

            System.out.printf("DLQ | count=%d%n", dlqCount.get());
            System.out.printf("Throughput: %.2f records/sec%n", throughput);
            System.out.println("================");
        }
    }
}
