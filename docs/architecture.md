High-Throughput Fan-Out Engine – Architecture Overview

1. Purpose
This system is designed to ingest large flat-file datasets (CSV/JSONL) and distribute each record
to multiple downstream systems in parallel while maintaining backpressure, rate limiting,
retry semantics, and zero data loss.

2. High-Level Architecture

Input File (CSV / JSONL)
        |
        v
Ingestion Layer (FileIngestor)
        |
        v
Bounded BlockingQueue (Backpressure)
        |
        v
FanOutOrchestrator (Virtual Threads)
   |          |           |           |
 REST Sink  gRPC Sink   Queue Sink   Wide-Column DB Sink

3. Component Responsibilities

A. Ingestion Layer (Producer)
- Reads input files line-by-line using BufferedReader
- Never loads the entire file into memory
- Pushes records into a bounded BlockingQueue
- If the queue is full, ingestion blocks, applying natural backpressure

B. Fan-Out Orchestrator
- Uses Java 21 Virtual Threads for high concurrency
- Each sink is processed independently in its own virtual thread
- Pulls records from the BlockingQueue
- Applies rate limiting, transformation, retry logic, and DLQ handling

C. Transformation Layer (Strategy Pattern)
- Each sink has its own Transformer implementation
- Allows new sinks/formats to be added without modifying the orchestrator

D. Throttling & Resilience
- Semaphore-based rate limiting per sink
- RetryExecutor retries transient failures up to a fixed max count
- Failed records are forwarded to a Dead Letter Queue (DLQ)

E. Observability
- MetricsReporter prints periodic status every 5 seconds:
  - Success count per sink
  - Failure count per sink
  - Dead Letter Queue size

4. Backpressure Strategy
- A bounded BlockingQueue ensures that slow sinks propagate pressure
  back to the file ingestion layer, preventing uncontrolled memory growth.

5. Scalability Model
- System scales linearly with CPU cores using lightweight Virtual Threads
- Blocking operations (queue, rate limiter) are safe and efficient

6. Extensibility
- Adding a new sink requires:
  - Implementing Sink
  - Implementing Transformer
  - Registering it in configuration
- Core orchestration logic remains unchanged
