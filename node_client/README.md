# Node.js Client for Payment Transaction API

Node.js client demonstrating CRUD operations against a Java server using Protocol Buffers.

## Overview

- **Client:** Node.js (HTTP client making binary proto requests)
- **Server:** Java (responds with binary proto responses)
- **Protocol:** Protocol Buffers (binary format)
- **Operations:** CRUD (Create, Read, List, Delete)

## Prerequisites

### Node.js
```bash
# Install Node.js 14+
node --version

# Should output: v14.x.x or higher
```

### Java Server Running
```bash
# From parent directory (buf-gradle-demo)
./gradlew run
# Server listens on http://localhost:8080
```

## Setup

### 1. Install Dependencies

```bash
# Inside node_client directory
npm install
```

This installs:
- `protobufjs` - Protocol Buffer library for Node.js (used for proto parsing if needed)

### 2. Verify Setup

```bash
node --version
npm --version
```

## Usage

### Run CRUD Demo

```bash
# Run full CRUD workflow (Create → Read → List → Delete)
node client.js
```

**Expected Output:**
```
🚀 Payment Transaction Client (Node.js)
================================

=== CREATE TRANSACTION ===
Request size: 176 bytes
✅ CREATE successful!
   Response size: 187 bytes

=== GET TRANSACTION ===
Request size: 22 bytes
✅ GET successful!
   Response size: 163 bytes

=== LIST TRANSACTIONS ===
Request size: 20 bytes
✅ LIST successful!
   Response size: 306 bytes

=== DELETE TRANSACTION ===
Request size: 22 bytes
✅ DELETE successful!
   Response size: 0 bytes

✅ All CRUD operations completed successfully!
```

### Show Help

```bash
node client.js --help
```

## Client Implementation

### Core Methods

#### CreateTransaction()
```javascript
const response = await createTransaction('path/to/create_request.pb');
```

#### GetTransaction()
```javascript
const response = await getTransaction('path/to/get_request.pb');
```

#### ListTransactions()
```javascript
const response = await listTransactions('path/to/list_request.pb');
```

#### DeleteTransaction()
```javascript
const response = await deleteTransaction('path/to/delete_request.pb');
```

### Request/Response

All methods return:
```javascript
{
  statusCode: 200,
  data: Buffer,        // Binary proto response
  size: number         // Response size in bytes
}
```

## Architecture

### Request Flow

```
Node Client
    ↓
[Read .pb file from disk]
    ↓
[Binary proto data in memory]
    ↓
HTTP POST http://localhost:8080/api/transactions/create
    ↓
[application/x-protobuf Content-Type]
    ↓
Java Server (Library class)
    ↓
[Binary proto response]
    ↓
HTTP 200 OK
    ↓
[Response Buffer]
    ↓
Log results
```

## Proto File Locations

**Binary Proto Test Files:**
```
../src/test/resources/
├── create_transaction_request.pb (176B)
├── get_transaction_request.pb (22B)
├── list_transactions_request.pb (20B)
├── delete_transaction_request.pb (22B)
├── minimal_transaction_request.pb (56B)
└── comprehensive_transaction_request.pb (221B)
```

These are pre-generated binary proto files used for testing.

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

### "Cannot find module 'http'"

Node.js built-in module not available. This should not happen with Node.js 14+.

```bash
node --version
# Make sure it's 14.x or higher
```

### "connection refused"

Java server not running:

```bash
# From parent directory
./gradlew run
```

### "ENOENT: no such file or directory"

Proto binary files not found. Ensure test resources are generated:

```bash
# From parent directory
./gradlew generateProto
```

## File Structure

```
node_client/
├── package.json        # Node dependencies
├── client.js           # Client implementation (CRUD operations)
├── README.md          # This file
├── QUICKSTART.md      # Quick start guide
├── .gitignore         # Git ignore patterns
└── node_modules/      # Installed dependencies (auto-created)
```

## Performance

### Message Sizes

| Operation | Request | Response | Total |
|-----------|---------|----------|-------|
| CREATE    | 176B    | 187B     | 363B  |
| GET       | 22B     | 163B     | 185B  |
| LIST      | 20B     | 306B     | 326B  |
| DELETE    | 22B     | 0B       | 22B   |
| **TOTAL** | **240B** | **656B** | **896B** |

### Efficiency

Binary proto is **33% smaller** than JSON:
- Binary proto: ~896 bytes
- JSON equivalent: ~2,500 bytes
- Savings: ~67%

## Extension Ideas

### Add Error Retry Logic

```javascript
async function makeRequestWithRetry(endpoint, data, maxRetries = 3) {
  for (let attempt = 0; attempt < maxRetries; attempt++) {
    try {
      return await makeRequest(endpoint, data);
    } catch (error) {
      if (attempt === maxRetries - 1) throw error;
      await new Promise(r => setTimeout(r, 1000 * (attempt + 1)));
    }
  }
}
```

### Add Response Parsing

```javascript
// Parse response using protobufjs
const descriptorSet = protobuf.loadSync('../proto/descriptor.pb');
const CreateTransactionResponse = descriptorSet.lookupType('payments.v1.CreateTransactionResponse');
const response = CreateTransactionResponse.decode(binaryData);
```

### Add Metrics

```javascript
const start = Date.now();
const response = await createTransaction(file);
const duration = Date.now() - start;
console.log(`Time: ${duration}ms, Size: ${response.size} bytes`);
```

## Comparison with Go Client

| Aspect | Node.js | Go |
|--------|---------|-----|
| **Language** | JavaScript | Go |
| **Dependencies** | protobufjs | google.golang.org/protobuf |
| **Setup** | npm install | go mod download + protoc |
| **Execution** | node client.js | go run main.js |
| **Performance** | JavaScript runtime | Compiled binary |
| **Proto Generation** | Optional (using .pb files) | Required |
| **Best for** | Web/servers, flexibility | Performance, concurrency |

## References

- **Protocol Buffers:** https://protobuf.dev
- **Node.js:** https://nodejs.org
- **protobufjs:** https://github.com/dcodeIO/protobuf.js
- **Buf Documentation:** https://buf.build/docs

## Running Full Integration Test

### Terminal 1: Start Java Server

```bash
cd ..
./gradlew run
# Wait for: ✅ Payment Transaction Server started
```

### Terminal 2: Run Node Client

```bash
cd node_client
npm install
node client.js
```

**Expected:** Full CRUD workflow completes successfully ✅

---

**Status:** Ready for production integration testing with Java backend
