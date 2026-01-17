# Apache Kafka Mastery with Spring Boot 🚀

Welcome to the ultimate guide for learning Apache Kafka through a real-world project approach. This repository contains a fully functional microservices ecosystem built with Java 21 and Spring Boot 3.4.x.

---

## 🧠 Part 1: Kafka Architecture Deep Dive

### What Problem does Kafka Solve?
In a traditional architecture, services are tightly coupled via REST/gRPC. If Service A is down, Service B fails. 
**Kafka** decoupling them by acting as a distributed, durable, and highly scalable **commit log**.

### Core Concepts

| Component | Description |
| :--- | :--- |
| **Broker** | A Kafka server. Multiple brokers form a **Cluster**. |
| **Topic** | A category/folder name where records are stored. |
| **Partition** | A Topic is split into partitions for parallelism. Partitions are ordered & immutable. |
| **Offset** | A unique ID for a message within a partition. |
| **Producer** | Sends data to Topics. |
| **Consumer** | Reads data from Topics. |
| **Consumer Group** | A group of consumers reading from a topic. Kafka ensures each partition is read by only ONE consumer in a group (Scaling). |

### 🛠️ The New Era: KRaft mode
Traditionally, Kafka required **Zookeeper** to manage metadata. In this project, we use **KRaft (Kafka Raft)**, where Kafka manages its own metadata via a Raft-based consensus protocol. It's faster, simpler, and more scalable.

### 🛡️ Reliability & Delivery Guarantees
1. **At-least-once (Default)**: Messages are never lost but may be duplicated.
2. **At-most-once**: Messages may be lost but never duplicated.
3. **Exactly-once (EOS)**: Achieved via **Idempotent Producers** and **Kafka Transactions**. (Used in our `Order Service`).

---

## 🏗️ Part 2: Project Architecture

We use an **E-Commerce Order Flow** to demonstrate production patterns:

1.  **Order Service (Producer)**:
    - Creates `OrderPlacedEvent`.
    - **Transactional**: Uses `@Transactional` to ensure message production is atomic.
    - **Idempotent**: Prevents duplicate writes automatically.
2.  **Payment Service (Consumer)**:
    - Simulates payment processing.
    - **Error Handling**: Uses **Exponential Backoff Retries** (4 attempts).
    - **Dead Letter Topic (DLT)**: Fails to a DLT if all retries fail.
3.  **Notification Service (Consumer)**:
    - Demonstrates independent consumption. Even if Payment Service fails, Notification Service continues processing.

---

## 🚀 Part 3: How to Run the Project

### 1. Prerequisites
- Docker & Docker Compose
- Java 21
- Maven

### 2. Start Kafka
```bash
docker-compose up -d
```

### 3. Build the Project
```bash
mvn clean install -DskipTests
```

### 4. Run Services (Separate Terminals)
```bash
# Order Service (Port 8081)
java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar

# Payment Service (Port 8082)
java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar

# Notification Service (Port 8083)
java -jar notification-service/target/notification-service-0.0.1-SNAPSHOT.jar
```

---

## 🧪 Part 4: Testing & Discovery

### A. Success Case
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8081/orders" -ContentType "application/json" -Body '{"customerId": "user1", "amount": 149.99}'
```
*Look at logs: Both Payment and Notification services will log the receipt synchronously.*

### B. Scalability Case
Start a second instance of `Payment Service`. Observe how Kafka **Rebalances** partitions among the instances in the `payment-group`.

### C. Failure & DLT Case
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8081/orders" -ContentType "application/json" -Body '{"customerId": "user2", "amount": 5.0, "status": "FAIL"}'
```
*Observe: The Payment Service will retry 3 times with increasing delays and then send the event to the DLT topic.*

---

## 🧐 Architect-Level Insights
- **Increasing Partitions**: Increases parallelism. However, you cannot decrease partitions. More partitions = more open file handles and higher rebalance time.
- **Failures**: Kafka handles broker failures via **Replication (ISRs)**. If a Leader dies, an In-Sync Replica (Follower) is promoted.
- **Common Mistake**: High `auto.offset.reset=earliest` in production. If a group ID is changed, you might re-process years of data!

---

**Summary**: Kafka is the backbone of modern event-driven architecture. Mastery of Partitions, Consumer Groups, and EOS is what separates a Senior Backend Engineer from the rest.
