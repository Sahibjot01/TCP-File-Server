# Concurrent TCP File Server with LRU Cache

A multithreaded TCP server implementation in Java that handles concurrent file requests with an in-memory LRU (Least Recently Used) cache for optimized performance.

## Features

- **Thread-safe LRU Cache**: O(1) access and eviction using HashMap + Doubly Linked List
- **Concurrent Client Handling**: Supports multiple simultaneous client connections via thread pool
- **Efficient File Caching**: Reduces disk I/O by storing frequently accessed files in memory
- **Performance Monitoring**: Built-in cache statistics (hits, misses, hit rate)

## Tech Stack

- Java 11+
- Multithreading & Concurrency (ReentrantLock, ThreadPool)
- Socket Programming (TCP)
- Custom Data Structures (Doubly Linked List)

## How It Works

1. Server listens on port 8010 for incoming TCP connections
2. Each client connection is handled by a separate thread from the thread pool
3. When a file is requested:
   - **Cache Hit**: File served directly from memory (~20-50ms)
   - **Cache Miss**: File loaded from disk, then cached for future requests
4. LRU eviction policy removes least recently used files when cache is full

## Project Structure

```
src/
├── cache/
│   ├── LRUCache.java       # LRU cache implementation
│   └── Node.java           # Doubly linked list node
├── server/
│   ├── Server.java         # TCP server
│   └── FileServer.java     # File serving logic
├── client/
│   └── FileClient.java     # Test client
└── files/
    └── file_1.txt to file_10.txt  # Test files
```

## Getting Started

### Prerequisites

- Java 11 or higher
- (Optional) JMeter for load testing
- (Optional) Telnet for manual testing

### Compiling the Project

```bash
# Compile all Java files
javac cache/*.java server/*.java client/*.java
```

This will generate `.class` files for all your source files.

### Running the Server

```bash
# Start the server
java server.Server
```

Server will start listening on `localhost:8010`

You should see output like:

```
Server started on port 8010
Waiting for clients...
```

### Testing the Server

#### Option 1: Using Telnet (Quick Manual Test)

````bash
# Connect to server
telnet localhost 8010


### Load Testing with JMeter

1. Open JMeter and create a Thread Group:

   - **Threads**: 50
   - **Ramp-up**: 10 seconds
   - **Loop Count**: 20

2. Add TCP Sampler pointing to `localhost:8010`

3. Expected Results:
   - Cache hit rate: 90%+
   - Average response time: 5-10ms

## Performance Metrics

| Metric                   | Value           |
| ------------------------ | --------------- |
| Cache Capacity           | 10 files        |
| Cache Hit Rate           | 90%+ under load |
| Concurrent Connections   | 50+             |
| Avg Response Time (Hit)  | ~20-50ms        |
| Avg Response Time (Miss) | ~100-200ms      |

## Configuration

Edit cache capacity in `Server.java`:

```java
LRUCache cache = new LRUCache(10); // Change capacity here
````

## What I Learned

- Thread synchronization and preventing race conditions with ReentrantLock
- Implementing custom data structures (doubly linked list with O(1) operations)
- Socket programming and handling concurrent TCP connections
- Thread pool management for efficient resource utilization
- Performance testing and optimization with JMeter
- Cache eviction policies and their trade-offs

## Architecture Highlights

### LRU Cache Implementation

- **HashMap**: O(1) key lookup
- **Doubly Linked List**: O(1) node movement for LRU ordering
- **ReentrantLock**: Thread-safe concurrent operations

### Concurrency Model

- Thread pool handles multiple client connections
- Each client request runs in isolated thread
- Lock-based synchronization prevents cache corruption

## License

MIT

---

**Note**: This project was built as a learning exercise to understand multithreading, caching strategies, and network programming in Java.
