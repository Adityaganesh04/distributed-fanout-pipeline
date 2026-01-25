# High-Throughput Fan-Out & Transformation Engine

## Overview
This project implements a **High-Throughput Fan-Out & Transformation Engine** in **Java 21**.  
It ingests large flat-file datasets (CSV/JSONL) and reliably distributes each record to multiple downstream systems in parallel.

The system simulates a real-world backend data pipeline where a **single source of truth** is transformed and delivered to heterogeneous sinks—such as REST APIs, gRPC services, message queues, and wide-column databases—while guaranteeing **backpressure, throttling, retries, and zero data loss**.

The solution leverages **Java 21 Virtual Threads**, **streaming I/O**, and the **Strategy Design Pattern** to achieve scalability, resilience, and extensibility.

---

## Key Features

- **Streaming Ingestion**
  - Reads input files line-by-line using `BufferedReader`
  - Capable of handling very large files (100GB+) without loading them into memory

- **Parallel Fan-Out**
  - Uses `Executors.newVirtualThreadPerTaskExecutor()` to fan out records to multiple sinks concurrently

- **Strategy-Based Transformations**
  - Per-sink transformation strategies:
    - REST → JSON
    - gRPC → Protobuf (simulated)
    - Message Queue → XML
    - Wide-Column DB → Avro/CQL-style map (simulated)

- **Resilience & Flow Control**
  - **Backpressure:** Bounded `BlockingQueue` prevents memory exhaustion if sinks slow down
  - **Rate Limiting:** Semaphore-based throttling (e.g., REST = 50 req/sec)
  - **Retry Logic:** Deterministic retry executor with **max 3 attempts**
  - **Dead Letter Queue (DLQ):** Failed records are captured for audit and replay

- **Observability**
  - Prints **per-sink success/failure counters, DLQ size and calculated throughput (records/sec)** every 5 seconds

- **Test Coverage**
  - Unit tests for transformers and retry logic
  - Integration test for orchestrator fan-out behavior using Mockito

---

## Architecture

### High-Level Data Flow

Input File (CSV / JSONL)
        |
        v
Ingestion Layer (Streaming Reader)
        |
        v
Bounded BlockingQueue (Backpressure)
(The bounded queue blocks the producer when sinks are slow,
preventing memory overload and enforcing flow control.)
        |
        v
Fan-Out Orchestrator (Virtual Threads)
        |        |        |        |
      REST     gRPC     Queue     DB
      Sink     Sink      Sink     Sink

---

## Module Breakdown

### 1. Ingestion Layer
**Class:** `FileIngestor`

- Reads input files line-by-line
- Pushes records into a bounded `BlockingQueue`
- Automatically slows down ingestion when downstream sinks are saturated

---

### 2. Transformation Layer (Strategy Pattern)

Each sink has an independent transformation strategy:

| Sink Type        | Transformer Class      | Output Format |
|------------------|-----------------------|---------------|
| REST API         | `JsonTransformer`     | JSON          |
| gRPC             | `ProtoTransformer`    | Protobuf-like |
| Message Queue    | `XmlTransformer`      | XML           |
| Wide-Column DB   | `AvroTransformer`     | Avro/CQL Map  |

Adding a new sink **does not require modifying the orchestrator**.

---

### 3. Throttling, Retry & DLQ

- **Rate Limiting**
  - Implemented using `Semaphore`
  - Threads block when limits are exceeded, naturally controlling throughput

- **Retry Executor**
  - Retries failed operations up to **3 times**
  - Designed for transient failures

- **Dead Letter Queue**
  - Records that fail after retries are stored in DLQ
  - Ensures **Zero Data Loss**

---

## Configuration

Configuration is externalized via `src/main/resources/application.json` and is used to define:

- Input file path
- Queue capacity (backpressure tuning)
- Per-sink rate limits (REST, gRPC, Queue, Wide-Column DB)

Retry behavior is intentionally fixed at a maximum of 3 attempts in code for clarity and determinism,
and can be externalized with minimal changes if required.

### Example Configuration (`application.json`)

```json
{
  "inputFile": "data.csv",
  "queueCapacity": 1000,
  "sinks": {
    "REST": { "rateLimit": 50 },
    "GRPC": { "rateLimit": 100 },
    "QUEUE": { "rateLimit": 500 },
    "WIDE_COLUMN_DB": { "rateLimit": 1000 }
  }
}
Note: Core wiring is intentionally explicit for clarity, while critical tunables are externalized.

Sample Input
This file exists solely for demonstration and testing; the engine supports arbitrarily large files without code changes.

The repository includes a small sample CSV file used for demonstration:

data.csv

id,name,age,country
1,Alice,29,India
2,Bob,35,USA
3,Charlie,41,UK
4,Deepak,27,India
5,Emily,32,Canada
This file can be replaced with larger datasets without any code changes.
The engine was locally tested with large CSV files (10k+ rows) to validate backpressure and throttling behavior.

How to Build & Run
Prerequisites
Java 21+

Maven 3.9+

Build
mvn clean package
Run
mvn exec:java
Ensure data.csv exists in the project root before running.

Running Tests
mvn test
Test Coverage Includes
Transformer correctness

Retry executor behavior

Orchestrator fan-out using mocked sinks (Mockito)

All tests pass with BUILD SUCCESS.

Design Decisions
Virtual Threads vs Reactive Frameworks
Virtual Threads provide high concurrency with significantly simpler code

Easier debugging and reasoning compared to reactive pipelines

BlockingQueue for Backpressure
Natural, predictable flow control

Prevents producer from overwhelming consumers

Semaphore-Based Rate Limiting
Lightweight, deterministic, and dependency-free

Suitable for backend ingestion pipelines

Explicit Failure Accounting
Every record ends in either success or DLQ

Guarantees zero silent data loss

AI Tooling Declaration
AI tools (ChatGPT and Cursor) were used to accelerate boilerplate generation and explore architectural patterns.

All core logic — including Virtual Thread orchestration, backpressure handling, retry semantics, DLQ design, and test validation — was manually implemented, reviewed, and validated.

A complete prompt history is included in Prompts.txt.