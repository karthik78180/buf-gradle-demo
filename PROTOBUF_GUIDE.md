# Protocol Buffers (Protobuf) Guide for Java Teams

## Table of Contents
1. [What is Protocol Buffers?](#what-is-protocol-buffers)
2. [Problems It Solves](#problems-it-solves)
3. [Key Advantages](#key-advantages)
4. [Why Buf?](#why-buf)
5. [Defining Proto Files](#defining-proto-files)
6. [Using Protobuf in Java](#using-protobuf-in-java)
7. [Generated Files Explained](#generated-files-explained)
8. [Building & Generating Code](#building--generating-code)
9. [Best Practices](#best-practices)

---

## What is Protocol Buffers?

**Protocol Buffers (protobuf)** is a method of serializing structured data developed by Google. It's a language-neutral, platform-neutral, extensible way to serialize data for network transmission, storage, and RPC communication.

Think of it as a **strongly-typed binary format** for data exchange - similar to JSON or XML, but more efficient and with built-in schema validation.

### Simple Example

Instead of sending JSON:
```json
{
  "id": "TXN-001",
  "storeName": "Starbucks",
  "amount": 99.99,
  "currency": "USD"
}
```

You send binary protobuf (more compact, faster parsing):
```
0x0a 0x07 0x54 0x58 0x4e 0x2d 0x30 0x30 0x31 0x12 0x0a 0x53 0x74 0x61 0x72 0x62 0x75 0x63 0x6b 0x73 0x19 0x04 0x66 0x66 0x66 0x66 0x66 0x66 0x66 0x18 0x58 0x4f
```

---

## Problems It Solves

| Problem | Traditional Approach | Protobuf Solution |
|---------|----------------------|-------------------|
| **Schema Evolution** | Manual versioning, breaking changes | Built-in backward/forward compatibility |
| **Data Size** | JSON/XML bloat (large payloads) | Binary encoding (45-65% smaller) |
| **Parsing Performance** | String parsing overhead | Direct binary deserialization |
| **Type Safety** | Runtime type checking | Compile-time type checking |
| **Code Generation** | Manual serialization/deserialization | Auto-generated classes & methods |
| **Cross-Language** | Language-specific formats | Works with Java, Go, Python, C++, etc. |
| **API Contracts** | Weak contracts (loose coupling) | Strong typed contracts |

---

## Key Advantages

### 1. **Small Payload Size** 📦
- **Result**: 45-65% smaller than JSON
- **Example**: Transaction data: 40 bytes (protobuf) vs 120 bytes (JSON)
- **Impact**: Faster network transfer, lower bandwidth costs

### 2. **Fast Serialization/Deserialization** ⚡
- No string parsing or JSON/XML interpretation
- Direct binary to object mapping
- **Performance**: 2-10x faster than JSON parsing

### 3. **Strong Schema Validation** ✓
- Compile-time type checking
- Prevents runtime serialization errors
- Clear data contracts between services

### 4. **Backward Compatible** 🔄
- Old clients can read data from new servers
- New clients can read data from old servers
- Add/remove fields without breaking existing code

### 5. **Auto-Generated Code** 🤖
- No manual serialization/deserialization
- Getters, setters, builders generated automatically
- Reduces boilerplate code

### 6. **Language Agnostic** 🌐
- Same proto file works with Java, Go, Python, C++, TypeScript, etc.
- Perfect for microservices architecture

---

## Why Buf?

**Buf** is a build system and CLI tool for Protocol Buffers. While `protoc` is the raw compiler, **Buf simplifies the workflow** for teams.

### Problems Buf Solves

| Traditional Protoc | With Buf |
|-------------------|----------|
| Manual dependency management | Automatic dependency resolution |
| No linting/validation | Built-in linting & best practices |
| Manual breaking change detection | Automatic breaking change detection |
| Complex CLI arguments | Simple configuration file (`buf.yaml`) |
| No format consistency | Auto-format proto files |

### Buf Benefits

1. **`buf.yaml` Configuration** - Single source of truth
2. **Automatic Linting** - Enforces naming conventions, best practices
3. **Breaking Change Detection** - Prevents API incompatibilities
4. **Module Management** - Handle dependencies cleanly
5. **Workspace Support** - Manage multiple proto modules
6. **Code Generation** - Cleaner output with `buf generate`

### In This Project

We use **Gradle plugin with Buf** for seamless Java integration:

```gradle
plugins {
    id "build.buf" version "0.7.0"
    id "com.google.protobuf" version "0.9.4"
}
```

This gives us:
- ✅ Automatic proto compilation during `./gradlew build`
- ✅ Built-in linting (`./gradlew bufLint`)
- ✅ Format checking (`./gradlew bufFormatCheck`)
- ✅ Generated Java classes automatically placed in build output

---

## Why protoc-gen-doc?

**protoc-gen-doc** is a plugin that generates **human-readable documentation** from your proto files.

### What It Does

- Converts `.proto` files → Markdown/HTML documentation
- Auto-generates API docs with field descriptions
- Includes message types, services, enum definitions
- Creates searchable documentation site

### When to Use

✅ **Generate API documentation** automatically
✅ **Share proto schemas** with non-developers
✅ **Create internal wikis** for data formats
✅ **Version documentation** alongside code

### In This Project

```bash
./gradlew generateProtoDocs
```

Generates markdown docs from your proto definitions, published to your team wiki.

---

## Defining Proto Files

### Basic Proto Syntax

Create `payments/v1/transaction.proto`:

```proto
// Always start with syntax version
syntax = "proto3";

// Package name (becomes Java package)
package payments.v1;

// Java options
option java_multiple_files = true;           // One class per message
option java_package = "com.example.payments.v1";
option java_outer_classname = "PaymentsProto";
option go_package = "go.example.com/payments/v1";  // For Go clients

// Message definition (like a Java class)
message Transaction {
  // Field: type name = field_number
  string id = 1;              // Field 1, unique ID
  string store_name = 2;      // Field 2, store name
  double amount = 3;          // Field 3, transaction amount
  string currency = 4;        // Field 4, currency code
  string location_zip = 5;    // Field 5, zip code
}

// Request message
message GetTransactionRequest {
  string store_name = 1;      // Input: store name to look up
}

// Response message
message GetTransactionResponse {
  Transaction transaction = 1;  // Output: transaction data
}

// Service definition (like a Java interface)
service TransactionService {
  // RPC method: input -> output
  rpc GetTransaction(GetTransactionRequest) returns (GetTransactionResponse) {}
}
```

### Key Concepts

| Concept | Explanation | Example |
|---------|-------------|---------|
| **Message** | Data structure (like a Java class) | `message Transaction { ... }` |
| **Field** | Property with type and number | `string id = 1;` |
| **Field Number** | Unique ID for each field (never change!) | `= 1, = 2, = 3` |
| **Service** | RPC interface (like a Java interface) | `service TransactionService { ... }` |
| **RPC** | Remote Procedure Call method | `rpc GetTransaction(...) returns (...)` |
| **Package** | Namespace (becomes Java package) | `package payments.v1;` |

### Field Types

```proto
// Scalar types
string id = 1;              // UTF-8 encoded string
int32 age = 2;              // 32-bit integer
int64 large_number = 3;     // 64-bit integer
double price = 4;           // Floating point
bool is_active = 5;         // Boolean
bytes data = 6;             // Raw bytes

// Collections
repeated string tags = 7;   // List of strings
repeated int32 ids = 8;     // List of integers

// Nested messages
Transaction transaction = 9;  // Another message type

// Enums
enum Status {
  UNKNOWN = 0;              // Always start at 0
  ACTIVE = 1;
  INACTIVE = 2;
}
Status status = 10;
```

### Backward Compatibility Rules

**Safe Changes** (won't break existing clients):
```proto
// ✅ Add new field with new number
message Transaction {
  string id = 1;
  string store_name = 2;
  double amount = 3;
  string currency = 4;
  string new_field = 5;     // OK - new clients will see it
}

// ✅ Mark field as deprecated (but don't remove)
message Transaction {
  string id = 1;
  string store_name = 2;
  double amount = 3 [deprecated = true];  // Old clients still work
}

// ✅ Add new service method
service TransactionService {
  rpc GetTransaction(...) returns (...) {}
  rpc ListTransactions(...) returns (...) {}  // New method, old clients ignore it
}
```

**Breaking Changes** (will break existing clients):
```proto
// ❌ Remove field
// ❌ Change field number
// ❌ Change field type (int32 to string)
// ❌ Rename field
// ❌ Remove service
```

---

## Using Protobuf in Java

### 1. Create Proto File

`src/main/proto/payments/v1/transaction.proto`:
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
```

### 2. Build Project

```bash
./gradlew build
```

This automatically:
- Compiles proto files
- Generates Java classes in `build/generated/source/proto/main/java/`

### 3. Use Generated Classes in Java

**Creating a message:**
```java
// Build a Transaction
Transaction transaction = Transaction.newBuilder()
    .setId("TXN-001")
    .setStoreName("Starbucks")
    .setAmount(99.99)
    .setCurrency("USD")
    .setLocationZip("12345")
    .build();
```

**Serializing to bytes:**
```java
// Convert to binary format
byte[] binary = transaction.toByteArray();
System.out.println("Binary size: " + binary.length + " bytes");  // ~40 bytes

// Send over network, write to database, etc.
```

**Deserializing from bytes:**
```java
// Parse binary data back to object
byte[] binaryData = receivedData;  // From network, database, etc.
Transaction transaction = Transaction.parseFrom(binaryData);

// Use the data
System.out.println("Store: " + transaction.getStoreName());
System.out.println("Amount: $" + transaction.getAmount());
```

**Sending in HTTP Request:**
```java
// Convert to binary
byte[] binary = transaction.toByteArray();

// Send as HTTP body with proper Content-Type
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("http://localhost:8080/api/transactions/get"))
    .header("Content-Type", "application/x-protobuf")
    .POST(HttpRequest.BodyPublishers.ofByteArray(binary))
    .build();

// Receive and parse response
HttpResponse<byte[]> response = client.send(request,
    HttpResponse.BodyHandlers.ofByteArray());

GetTransactionResponse resp = GetTransactionResponse
    .parseFrom(response.body());
```

---

## Generated Files Explained

When you run `./gradlew build`, Gradle generates Java files in `build/generated/source/proto/main/java/`:

### Example Generated File: `Transaction.java`

```java
public final class Transaction extends
    com.google.protobuf.GeneratedMessageV3 implements
    TransactionOrBuilder {

  // Field access methods
  public String getId() { ... }
  public String getStoreName() { ... }
  public double getAmount() { ... }
  public String getCurrency() { ... }
  public String getLocationZip() { ... }

  // Serialization
  public byte[] toByteArray() { ... }        // Convert to binary
  public static Transaction parseFrom(byte[] data) { ... }

  // Builder pattern
  public static Builder newBuilder() { ... }

  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> {
    public Builder setId(String value) { ... }
    public Builder setStoreName(String value) { ... }
    public Builder setAmount(double value) { ... }
    public Transaction build() { ... }
  }

  // Utility methods
  public boolean equals(Object obj) { ... }
  public int hashCode() { ... }
  public String toString() { ... }
}
```

### What You Get Automatically

| Method | Purpose |
|--------|---------|
| `getId()`, `getStoreName()`, etc. | Getter methods |
| `newBuilder()` | Create builder for object construction |
| `toByteArray()` | Serialize to binary |
| `parseFrom(byte[])` | Deserialize from binary |
| `equals()`, `hashCode()`, `toString()` | Standard Java methods |
| `getDescriptor()` | Metadata about the message |

### Generated Service Files

For services, you also get:

```java
public interface TransactionServiceGrpc {
  // Async stub for client-side calls
  public static class TransactionServiceStub extends ...

  // Blocking stub for synchronous calls
  public static class TransactionServiceBlockingStub extends ...

  // Server-side implementation interface
  public static class TransactionServiceImplBase extends ...
}
```

---

## Building & Generating Code

### Using Gradle (Recommended)

**`build.gradle`:**
```gradle
plugins {
    id "build.buf" version "0.7.0"           // Buf linting
    id "com.google.protobuf" version "0.9.4" // Protoc compilation
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.0"
    }
}
```

**Commands:**

```bash
# Generate Java classes from proto
./gradlew generateProto

# Lint proto files for best practices
./gradlew bufLint

# Check if proto files are formatted correctly
./gradlew bufFormatCheck

# Auto-format proto files
./gradlew bufFormatApply

# Generate documentation from proto
./gradlew generateProtoDocs

# Full build (includes proto generation)
./gradlew build

# Clean generated files
./gradlew clean
```

### Proto File Layout

```
src/
├── main/
│   └── proto/
│       └── payments/v1/
│           ├── transaction.proto       # Main messages
│           ├── payment_common.proto    # Shared types
│           └── service.proto           # Service definitions
├── java/
│   └── com/example/
│       ├── app/App.java               # Application code
│       └── service/TransactionService.java
└── test/
    └── java/
        └── com/example/
            └── LibraryTest.java        # Tests
```

---

## Best Practices

### ✅ DO

1. **Use explicit field numbers** - Always use specific numbers (1, 2, 3, etc.), never reuse
   ```proto
   message Good {
     string id = 1;
     string name = 2;
     // Never use 3 for something else after deleting old_field
   }
   ```

2. **Use builder pattern** - Easier to read and maintain
   ```java
   Transaction txn = Transaction.newBuilder()
       .setId("TXN-001")
       .setAmount(99.99)
       .build();
   ```

3. **Organize by version** - Keep proto files in versioned packages
   ```
   payments/v1/transaction.proto
   payments/v2/transaction.proto  # If API changes
   ```

4. **Add documentation** - Comment your proto definitions
   ```proto
   message Transaction {
     // Unique transaction ID (generated)
     string id = 1;
     // Store name where transaction occurred
     string store_name = 2;
   }
   ```

5. **Use enums for constants**
   ```proto
   enum CurrencyCode {
     UNKNOWN_CURRENCY = 0;
     USD = 1;
     EUR = 2;
   }
   ```

### ❌ DON'T

1. **Don't reuse field numbers** - This breaks backward compatibility
   ```proto
   // ❌ BAD
   message Bad {
     string id = 1;
     // removed: string old_field = 2;
     string new_field = 2;  // REUSING 2 - BREAKS EVERYTHING!
   }
   ```

2. **Don't modify field types**
   ```proto
   // ❌ BAD - Changing int32 to string breaks old clients
   // Before: int32 amount = 1;
   // After:  string amount = 1;
   ```

3. **Don't use repeated without understanding implications**
   ```proto
   // Be careful with large repeated fields
   repeated Transaction transactions = 1;  // OK for small lists
   repeated Transaction transactions = 1;  // Careful: 1M items = huge message
   ```

4. **Don't forget oneof for exclusive fields**
   ```proto
   message Payment {
     // User pays with either credit card OR bank account
     oneof payment_method {
       CreditCard credit_card = 1;
       BankAccount bank_account = 2;
     }
   }
   ```

### Version Management

For API evolution, version your proto files:

```
v1/ - Initial release (stable)
v2/ - New fields added (backward compatible)
v3/ - Breaking changes (new major version)
```

In Java packages:
```java
com.example.payments.v1.*;  // Import from v1
com.example.payments.v2.*;  // Import from v2 if needed
```

---

## Real-World Example

### Scenario: Payment Transaction API

**Proto Definition:**
```proto
// payments/v1/transaction.proto
syntax = "proto3";
package payments.v1;

option java_multiple_files = true;
option java_package = "com.example.payments.v1";

enum CurrencyCode {
  UNKNOWN_CURRENCY = 0;
  USD = 1;
  EUR = 2;
}

message Transaction {
  string id = 1;
  string store_name = 2;
  double amount = 3;
  CurrencyCode currency = 4;
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

**Java Implementation:**
```java
// Server: Handle request
GetTransactionRequest request = GetTransactionRequest
    .parseFrom(requestBytes);

String storeName = request.getStoreName();

// Create response
Transaction transaction = Transaction.newBuilder()
    .setId("TXN-" + System.currentTimeMillis())
    .setStoreName(storeName)
    .setAmount(99.99)
    .setCurrency(CurrencyCode.USD)
    .setLocationZip("12345")
    .build();

GetTransactionResponse response = GetTransactionResponse
    .newBuilder()
    .setTransaction(transaction)
    .build();

// Send back as binary
byte[] responseBytes = response.toByteArray();
```

**Binary Size Comparison:**
```
Protobuf:   ~50 bytes
JSON:       ~150 bytes  (3x larger)
XML:        ~200 bytes  (4x larger)

Savings: 65% smaller than JSON!
```

---

## Quick Reference

| Task | Command |
|------|---------|
| Generate Java classes | `./gradlew generateProto` |
| Lint proto files | `./gradlew bufLint` |
| Format proto files | `./gradlew bufFormatApply` |
| Generate docs | `./gradlew generateProtoDocs` |
| Full build | `./gradlew build` |
| Clean generated files | `./gradlew clean` |

---

## Resources

- [Protocol Buffers Documentation](https://developers.google.com/protocol-buffers)
- [Buf Documentation](https://buf.build/docs)
- [Java Protobuf API](https://developers.google.com/protocol-buffers/docs/reference/java-generated)
- [Proto3 Language Guide](https://developers.google.com/protocol-buffers/docs/proto3)

---

## Next Steps

1. **Start small** - Define one proto message for existing domain model
2. **Generate code** - Run `./gradlew generateProto` and explore generated files
3. **Test serialization** - Write unit tests for serialization/deserialization
4. **Integrate with services** - Use in one microservice first, then expand
5. **Version properly** - Plan for API evolution from day one

---

**Questions?** Reach out to the platform team or check the [example code](./EXAMPLE_USAGE.md) in this repo.
