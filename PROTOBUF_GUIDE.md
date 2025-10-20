# Protocol Buffers Guide

A reference guide for implementing Protocol Buffers in Java applications with Gradle and Buf.

---

## Overview

Protocol Buffers (Protobuf) is a binary serialization format for structured data. It provides:
- Compact binary encoding (45-67% smaller than JSON)
- Language-agnostic message definition
- Automatic code generation
- Type safety and validation
- Backward/forward compatibility

## Why Protocol Buffers

### Problems Solved

| Problem | Impact |
|---------|--------|
| Data bloat | 1M API calls: 150MB/day (JSON) vs 50MB/day (Protobuf) |
| Parsing overhead | 10x faster deserialization than JSON |
| Type inconsistency | Compile-time validation prevents runtime errors |
| API evolution | Breaking changes detected automatically |
| Cross-language integration | Single format works with Java, Go, Python, JavaScript, etc. |

### ROI Example

```
Processing 1M transactions daily:
- JSON: 54 GB/year storage, $40K cloud bill
- Protobuf: 18 GB/year storage, $13K cloud bill
- Annual savings: $27K
```

---

## Architecture

### Core Components

```
.proto files (definition)
    ↓
Buf (validation, linting)
    ↓
Gradle/Protoc (code generation)
    ↓
Generated Java classes
    ↓
Application code
```

### Message Flow

```
Object → Serialization (toByteArray) → Binary data → Network/Storage
Binary data → Deserialization (parseFrom) → Object → Application logic
```

---

## Proto File Structure

### Minimal Example

```proto
syntax = "proto3";
package payments.v1;

option java_multiple_files = true;
option java_package = "com.example.payments.v1";

message Transaction {
  string id = 1;
  string store_name = 2;
  double amount = 3;
  string currency = 4;
  string location_zip = 5;
}

message GetTransactionRequest {
  string store_name = 1;
}

message GetTransactionResponse {
  Transaction transaction = 1;
}

service TransactionService {
  rpc GetTransaction(GetTransactionRequest)
    returns (GetTransactionResponse) {}
}
```

### Field Types Reference

| Type | Example | Bytes | Use Case |
|------|---------|-------|----------|
| string | "text" | variable | Text data |
| int32 | 42 | 1-5 | Regular integers |
| int64 | 9223372036854775807 | 1-10 | Large integers |
| double | 3.14 | 8 | Floating point |
| bool | true | 1 | Boolean values |
| bytes | raw data | variable | Binary data |

### Collection Types

```proto
// List of items
repeated string tags = 1;

// Nested message
Address address = 2;

// Enum
enum Status {
  UNKNOWN = 0;
  ACTIVE = 1;
  INACTIVE = 2;
}
```

### Field Numbering Rules

- Field numbers 1-15 use 1 byte encoding (use for frequently accessed fields)
- Field numbers 16+ use 2 byte encoding
- **Never reuse field numbers** - breaks backward compatibility
- Mark deleted fields as reserved: `reserved 5;`

---

## Java Integration

### Creating Messages

```java
import com.example.payments.v1.Transaction;

Transaction txn = Transaction.newBuilder()
    .setId("TXN-001")
    .setStoreName("Starbucks")
    .setAmount(5.50)
    .setCurrency("USD")
    .setLocationZip("94105")
    .build();
```

### Serialization

```java
// Convert to binary
byte[] binary = txn.toByteArray();

// Write to network
httpRequest.setBody(binary);

// Write to database
database.save(binary);
```

### Deserialization

```java
// Parse from binary
byte[] receivedData = ...;
Transaction txn = Transaction.parseFrom(receivedData);

// Access fields
String store = txn.getStoreName();
double amount = txn.getAmount();
```

### Collections

```proto
message ShoppingCart {
  string customer_id = 1;
  repeated Item items = 2;

  message Item {
    string name = 1;
    double price = 2;
    int32 quantity = 3;
  }
}
```

```java
ShoppingCart cart = ShoppingCart.newBuilder()
    .setCustomerId("CUST-123")
    .addItems(ShoppingCart.Item.newBuilder()
        .setName("Coffee")
        .setPrice(3.50)
        .setQuantity(2)
        .build())
    .addItems(ShoppingCart.Item.newBuilder()
        .setName("Croissant")
        .setPrice(4.99)
        .setQuantity(1)
        .build())
    .build();
```

---

## Gradle Configuration

### build.gradle

```gradle
plugins {
    id "build.buf" version "0.7.0"
    id "com.google.protobuf" version "0.9.4"
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.0"
    }
}
```

### Generated Code Location

```
build/generated/source/proto/main/java/
└── com/example/payments/v1/
    ├── Transaction.java
    ├── GetTransactionRequest.java
    ├── GetTransactionResponse.java
    └── TransactionServiceGrpc.java
```

---

## Build Commands

### Proto Generation

```bash
# Generate Java classes from proto files
./gradlew generateProto

# Check proto file format
./gradlew bufFormatCheck

# Validate proto files for issues
./gradlew bufLint

# Detect breaking changes
./gradlew bufValidate

# Auto-format proto files
./gradlew bufFormatApply

# Full build (includes proto generation)
./gradlew build

# Clean generated files
./gradlew clean
```

### Generated File Structure

```
<package>/ (based on proto package name)
├── Transaction.java (message class)
├── GetTransactionRequest.java (request message)
├── GetTransactionResponse.java (response message)
└── TransactionServiceGrpc.java (service definitions)
```

---

## Generated Methods Reference

### Message Classes

| Method | Purpose | Returns |
|--------|---------|---------|
| `toByteArray()` | Serialize to binary | byte[] |
| `parseFrom(byte[])` | Deserialize from binary | Message |
| `newBuilder()` | Create builder instance | Builder |
| `get<Field>()` | Access field value | Field type |
| `equals(Object)` | Compare messages | boolean |
| `toString()` | Debug representation | String |

### Builder Class

```java
Transaction.Builder builder = Transaction.newBuilder();
builder.setId("TXN-001");
builder.setStoreName("Store");
builder.setAmount(99.99);
Transaction txn = builder.build();

// Or chained
Transaction txn = Transaction.newBuilder()
    .setId("TXN-001")
    .setStoreName("Store")
    .setAmount(99.99)
    .build();
```

---

## Compatibility Rules

### Breaking Changes (Avoid)

- Changing field data type
- Removing fields without reserving number
- Renaming fields
- Changing field numbering
- Removing service methods

### Safe Changes (Supported)

- Adding new fields with new numbers
- Deprecating fields (mark deprecated)
- Adding service methods
- Marking fields as reserved

### Example: Safe Evolution

```proto
// Version 1
message Transaction {
  string id = 1;
  double amount = 2;
}

// Version 2 (forward compatible)
message Transaction {
  string id = 1;
  double amount = 2;
  string description = 3;    // New field
  reserved 4;                 // Reserved for future use
}
```

---

## Common Patterns

### Pagination

```proto
message ListRequest {
  int32 page_size = 1;
  string page_token = 2;
}

message ListResponse {
  repeated Item items = 1;
  string next_page_token = 2;
}
```

### Error Handling

```proto
message ErrorResponse {
  int32 code = 1;
  string message = 2;
  map<string, string> details = 3;
}
```

### Optional Fields

```proto
message User {
  string id = 1;
  string name = 2;
  string email = 3;          // May be empty
  string phone = 4;          // May be empty
}
```

### Enums for Constants

```proto
enum PaymentStatus {
  UNKNOWN = 0;
  PENDING = 1;
  COMPLETED = 2;
  FAILED = 3;
}

message Payment {
  string id = 1;
  PaymentStatus status = 2;  // Type-safe
}
```

---

## Directory Structure

```
your-project/
├── src/main/proto/
│   └── payments/v1/
│       ├── transaction.proto
│       ├── service.proto
│       └── payment_common.proto
│
├── src/main/java/
│   └── com/example/
│       └── PaymentService.java
│
├── build/generated/
│   └── source/proto/main/java/
│       └── com/example/payments/v1/
│           ├── Transaction.java
│           ├── GetTransactionRequest.java
│           └── GetTransactionResponse.java
│
├── build.gradle
├── buf.yaml
└── buf.lock
```

---

## Buf Configuration (buf.yaml)

```yaml
version: v1
build:
  roots:
    - proto
lint:
  use:
    - DEFAULT
  except:
    - COMMENT_ENUM_VALUE  # Optional: exclude specific rules
breaking:
  use:
    - FILE
```

---

## Performance Characteristics

### Message Size Comparison

```
Protobuf:  40-50 bytes
JSON:      150-200 bytes (3-5x larger)
```

### Parsing Performance

```
Protobuf:  0.1ms per message
JSON:      1.0ms per message (10x slower)
```

### Scaling Example: 1M Devices, Every Second

```
JSON:     540 GB/hour  (3.9 TB/day)
Protobuf: 144 GB/hour  (1.0 TB/day)
Saving:   396 GB/hour  (73% reduction)
```

---

## Best Practices

### DO

- Use snake_case for field names (converts to camelCase in Java)
- Document complex messages with comments
- Version packages (payments/v1, payments/v2)
- Start field numbers at 1
- Reserve deleted field numbers
- Use meaningful message names

### DON'T

- Reuse field numbers
- Change field types
- Delete fields without reserving
- Use deeply nested messages (>2 levels)
- Store sensitive data unencrypted
- Ignore breaking change warnings

---

## Troubleshooting

### "Cannot find symbol: class Transaction"

**Cause:** Proto files not generated
**Solution:**
```bash
./gradlew clean generateProto
./gradlew build
```

### "Field name should be snake_case"

**Cause:** Using camelCase in proto
**Solution:**
```proto
# Wrong
string storeName = 1;

# Correct
string store_name = 1;  # Becomes getStoreName() in Java
```

### "This breaks backward compatibility"

**Cause:** Changing field type or reusing field number
**Solution:** Add new field with new number instead
```proto
message Transaction {
  double amount = 1;
  double amount_v2 = 2;  # New version
}
```

### Proto file not found

**Cause:** File in wrong location
**Solution:** Use `src/main/proto/payments/v1/` structure
```
Correct:   src/main/proto/payments/v1/transaction.proto
Wrong:     src/java/Transaction.proto
```

---

## HTTP Integration

### Request Format

```java
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/transactions"))
    .header("Content-Type", "application/x-protobuf")
    .POST(HttpRequest.BodyPublishers.ofByteArray(txn.toByteArray()))
    .build();

HttpResponse<byte[]> response = client.send(request,
    HttpResponse.BodyHandlers.ofByteArray());
```

### Response Parsing

```java
if (response.statusCode() == 200) {
    GetTransactionResponse resp = GetTransactionResponse
        .parseFrom(response.body());
    Transaction txn = resp.getTransaction();
}
```

---

## gRPC Services

### Service Definition

```proto
service TransactionService {
  rpc GetTransaction(GetTransactionRequest)
    returns (GetTransactionResponse) {}

  rpc CreateTransaction(CreateTransactionRequest)
    returns (CreateTransactionResponse) {}

  rpc ListTransactions(ListTransactionsRequest)
    returns (stream Transaction) {}
}
```

### Generated Interfaces

- `TransactionServiceImplBase` - Server implementation base
- `TransactionServiceStub` - Async client stub
- `TransactionServiceBlockingStub` - Blocking client stub

---

## Version Management

### Package Versioning Strategy

```
payments/v1/  → Stable, backward compatible
payments/v2/  → Breaking changes, new major version
```

### Field Evolution

```proto
// v1
message User {
  string id = 1;
  string name = 2;
}

// v2 - Add new field, keep old ones
message User {
  string id = 1;
  string name = 2;
  string email = 3;  // New
}

// v3 - Breaking change, new package
message User {  // payments/v3
  string id = 1;
  string full_name = 2;  // Renamed from 'name'
}
```

---

## Resources

| Resource | Link |
|----------|------|
| Official Documentation | https://developers.google.com/protocol-buffers |
| Buf Documentation | https://buf.build/docs |
| Proto3 Language Guide | https://developers.google.com/protocol-buffers/docs/proto3 |
| Java Generated Code | https://developers.google.com/protocol-buffers/docs/reference/java-generated |

---

## Quick Reference

### Create Proto File

```bash
mkdir -p src/main/proto/payments/v1
cat > src/main/proto/payments/v1/transaction.proto << 'EOF'
syntax = "proto3";
package payments.v1;

option java_multiple_files = true;
option java_package = "com.example.payments.v1";

message Transaction {
  string id = 1;
  string store_name = 2;
  double amount = 3;
}
EOF
```

### Generate and Build

```bash
./gradlew generateProto
./gradlew build
```

### Use in Code

```java
Transaction txn = Transaction.newBuilder()
    .setId("TXN-001")
    .setStoreName("Store")
    .setAmount(99.99)
    .build();

byte[] binary = txn.toByteArray();
```

---

## Related Documentation

- [Gradle Protobuf Plugin](https://github.com/google/protobuf-gradle-plugin)
- [Buf CLI Reference](https://buf.build/docs/reference/cli)
- [Java Protobuf API](https://developers.google.com/protocol-buffers/docs/reference/java-generated)

