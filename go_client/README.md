# Go Client for Payment Transaction API

Go client demonstrating CRUD operations against a Java server using Protocol Buffers.

## Overview

- **Client:** Go (HTTP client making binary proto requests)
- **Server:** Java (responds with binary proto responses)
- **Protocol:** Protocol Buffers (binary format)
- **Operations:** CRUD (Create, Read, Update, Delete)

## Prerequisites

### Go
```bash
# Install Go 1.21+
go version

# Should output: go version go1.21.x ...
```

### Java Server Running
```bash
# From parent directory (buf-gradle-demo)
./gradlew run
# Server listens on http://localhost:8080
```

## Setup

### 1. Generate Go Proto Bindings

First, generate Go code from proto files:

```bash
# From go_client directory
./setup.sh
```

Or manually:

```bash
# Install protoc (if not already installed)
# macOS:
brew install protobuf

# Or download from: https://github.com/protocolbuffers/protobuf/releases

# Install Go proto compiler
go install github.com/golang/protobuf/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest

# Generate Go code from proto files
protoc \
  -I../src/main/proto \
  --go_out=gen \
  ../src/main/proto/payments/v1/payment_common.proto \
  ../src/main/proto/payments/v1/transaction.proto
```

**Output:** `gen/payments/v1/` directory with Go proto bindings

### 2. Install Dependencies

```bash
go mod download
```

## Usage

### Run CRUD Demo

```bash
# Run full CRUD workflow (Create → Read → List → Delete)
go run main.go
```

**Expected Output:**
```
🚀 Payment Transaction Client (Go)
================================

=== CREATE TRANSACTION ===
Request size: 123 bytes
Transaction ID: TXN-001
Amount: $150.00 USD
✅ Transaction created successfully
   Create Time: 2024-10-20T...
   Update Time: 2024-10-20T...

=== GET TRANSACTION ===
Request size: 15 bytes
Looking up: transactions/TXN-001
✅ Transaction retrieved successfully
   ID: TXN-001
   Amount: $150.00 USD

=== LIST TRANSACTIONS ===
Request size: 10 bytes
Page size: 10
✅ Transactions listed successfully
   Total transactions: 2
   [1] ID: TXN-001, Amount: $150.00 USD
   [2] ID: TXN-002, Amount: $99.99 USD

=== DELETE TRANSACTION ===
Request size: 14 bytes
Deleting: transactions/TXN-001
✅ Transaction deleted successfully

✅ All CRUD operations completed successfully!
```

### Show Help

```bash
go run main.go help
```

## Architecture

### Request Flow

```
Go Client
    ↓
[Create Transaction Request (binary proto)]
    ↓
HTTP POST http://localhost:8080/api/transactions/create
    ↓
Java Server (Library class)
    ↓
[Create Transaction Response (binary proto)]
    ↓
Go Client
    ↓
Display Results
```

### Supported Operations

#### CREATE - Create Transaction
```go
CreateTransaction(&pb.Transaction{
    Id:       "TXN-001",
    Amount:   150.00,
    Currency: "USD",
    Cardholder: {...},
    Store:      {...},
})
```

**Request Size:** ~176 bytes (binary proto)

**Response:** `CreateTransactionResponse` with created transaction + timestamps

#### READ - Get Transaction
```go
GetTransaction("TXN-001")
```

**Request Size:** ~15 bytes (binary proto)

**Response:** `GetTransactionResponse` with transaction details

#### LIST - List Transactions
```go
ListTransactions(10) // pageSize: 10
```

**Request Size:** ~10 bytes (binary proto)

**Response:** `ListTransactionsResponse` with array of transactions

#### DELETE - Delete Transaction
```go
DeleteTransaction("TXN-001")
```

**Request Size:** ~14 bytes (binary proto)

**Response:** `DeleteTransactionResponse` (empty)

## Proto File Locations

**Proto Definitions:**
```
../src/main/proto/
├── payments/v1/
│   ├── payment_common.proto    (Address, CardHolder, Store)
│   └── transaction.proto       (Transaction, Request/Response messages)
└── schema/service/v1/
    └── service.proto           (Service definition)
```

**Generated Go Code:**
```
gen/
└── payments/
    └── v1/
        ├── payment_common.pb.go
        └── transaction.pb.go
```

## Content-Type

All requests use:
```
Content-Type: application/x-protobuf
```

**Why x-protobuf?**
- Standard MIME type for Protocol Buffers
- Explicitly declares binary proto format
- Better than generic `application/octet-stream`

## Troubleshooting

### "cannot find package gen/payments/v1"

Proto bindings not generated. Run setup:

```bash
./setup.sh
# or manually:
protoc -I../src/main/proto --go_out=gen \
  ../src/main/proto/payments/v1/*.proto
```

### "connection refused"

Java server not running:

```bash
# From parent directory
./gradlew run
```

### "server error: 400"

Proto serialization mismatch. Verify:

1. Proto definitions match between Go and Java
2. Field numbers haven't changed
3. Required fields are set

## File Structure

```
go_client/
├── go.mod                 # Go module definition
├── main.go               # Client implementation (CRUD operations)
├── README.md            # This file
├── setup.sh             # Auto-setup script
├── .gitignore           # Go build artifacts
└── gen/                 # Generated Go proto code (auto-created)
    └── payments/v1/
        ├── payment_common.pb.go
        └── transaction.pb.go
```

## Protocol Buffer Binary Format

**Why binary proto over JSON?**

| Aspect | Binary Proto | JSON |
|--------|------------|------|
| Size | 176B | 350B+ |
| Performance | Fast | Slower |
| Bandwidth | Efficient | High |
| Readability | Binary | Human-readable |
| Parsing | Native | Parse + convert |

Binary proto is **33% smaller** and **faster** for production APIs.

## Extension Ideas

### Add JWT Authentication

```go
req.Header.Add("Authorization", "Bearer <token>")
```

### Add Timeout

```go
client := &http.Client{Timeout: 5 * time.Second}
```

### Add Retry Logic

```go
for attempt := 0; attempt < 3; attempt++ {
    if err := request(); err == nil {
        break
    }
    time.Sleep(time.Second * time.Duration(attempt))
}
```

### Add Metrics

```go
fmt.Printf("Request took %v\n", time.Since(start))
fmt.Printf("Response size: %d bytes\n", len(respBody))
```

## References

- **Buf Documentation:** https://buf.build/docs
- **Protocol Buffers:** https://protobuf.dev
- **Go Protobuf:** https://pkg.go.dev/google.golang.org/protobuf
- **gRPC:** https://grpc.io
- **Proto3 Spec:** https://protobuf.dev/programming-guides/proto3

## Running Full Integration Test

### Terminal 1: Start Java Server

```bash
cd ..
./gradlew run
# Wait for: Server is running...
```

### Terminal 2: Run Go Client

```bash
cd go_client
go run main.go
```

**Expected:** Full CRUD workflow completes successfully ✅

---

**Status:** Ready for production integration testing with Java backend
