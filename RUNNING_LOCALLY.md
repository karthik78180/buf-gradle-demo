# Running the Application Locally

This guide explains how to build, run, and test the Payment Transaction API locally using proto binary serialization.

## Prerequisites

- **Java 21+** installed
- **Gradle 9.0.0+** (included via wrapper)
- **Git** for version control
- **Postman** or **curl** for testing APIs

## Build the Application

### Step 1: Clone and Build

```bash
# Clone the repository
git clone https://github.com/karthik78180/buf-gradle-demo.git
cd buf-gradle-demo

# Build with validation and tests
./gradlew clean build
```

**Expected Output:**
```
BUILD SUCCESSFUL in 1s
18 actionable tasks: 18 executed
```

### Step 2: Verify Proto Generation

```bash
# Check generated Java code
ls -la build/generated/sources/proto/main/java/com/example/payments/v1/

# Check generated documentation
ls -la build/generated/sources/proto/main/doc/
```

## Run Tests Locally

### Run All Tests

```bash
./gradlew test
```

**Expected Output:**
```
Library - Proto Binary Processing Tests
  23 tests completed, 0 failed
```

### Run Specific Test Category

```bash
# Run proto validation tests only
./gradlew test --tests "*ValidationTest"

# Run processing tests
./gradlew test --tests "*Processing*"
```

## Understanding Proto Binary Format

### What is Proto Binary?

Protocol Buffers serialize messages into compact binary format:
- **Efficient**: Smaller than JSON/XML
- **Typed**: Message structure is enforced
- **Language-agnostic**: Can use any language

### Example: Create Transaction Request

**Proto Definition:**
```proto
message Transaction {
  string id = 1;
  double amount = 2;
  string currency = 3;
  CardHolder cardholder = 4;
  Store store = 5;
}

message CreateTransactionRequest {
  string parent = 1;
  Transaction transaction = 2;
}
```

**JSON Equivalent:**
```json
{
  "parent": "merchants/M-001",
  "transaction": {
    "id": "TXN-001",
    "amount": 100.00,
    "currency": "USD",
    "cardholder": {
      "cardholder_name": "John Doe",
      "email": "john@example.com"
    },
    "store": {
      "store_id": "STORE-001",
      "store_name": "Seattle Store"
    }
  }
}
```

**Proto Binary:**
```
0a 0b 6d 65 72 63 68 61 6e 74 73 2f 4d 2d 30 30 31 12 ...
```

## Generate Proto Binary Data

### Pre-Generated Test Files (Easiest Option!)

The project includes pre-generated proto binary files ready to use with Postman or curl:

**Location:** `src/test/resources/`

**Available Files:**

| File | Size | Use Case |
|------|------|----------|
| `create_transaction_request.pb` | 176B | Full transaction creation |
| `get_transaction_request.pb` | 22B | Get transaction by ID |
| `list_transactions_request.pb` | 20B | List transactions with pagination |
| `delete_transaction_request.pb` | 22B | Delete transaction |
| `minimal_transaction_request.pb` | 56B | Minimal required fields only |
| `comprehensive_transaction_request.pb` | 221B | All fields populated |
| `create_transaction_request_sample.pb` | 56B | Sample transaction |

**Quick Start with curl:**
```bash
# Use pre-generated binary file
curl -X POST http://localhost:8080/api/transactions/create \
  -H "Content-Type: application/x-protobuf" \
  --data-binary @src/test/resources/create_transaction_request.pb \
  -o response.pb
```

**Quick Start with Postman:**
1. Click **Body** → **Binary**
2. Click **Select File**
3. Choose: `src/test/resources/create_transaction_request.pb`
4. Send request

### Generate Your Own Proto Binary Data

#### Using Java Code (Recommended for Testing)

```java
import com.example.payments.v1.*;

// Create transaction
Transaction transaction = Transaction.newBuilder()
    .setId("TXN-001")
    .setAmount(100.00)
    .setCurrency("USD")
    .setCardholder(CardHolder.newBuilder()
        .setCardholderName("John Doe")
        .setEmail("john@example.com")
        .build())
    .setStore(Store.newBuilder()
        .setStoreId("STORE-001")
        .setStoreName("Seattle Store")
        .build())
    .build();

// Create request
CreateTransactionRequest request = CreateTransactionRequest.newBuilder()
    .setParent("merchants/M-001")
    .setTransaction(transaction)
    .build();

// Get binary data
byte[] binaryData = request.toByteArray();

// Convert to Base64 for transmission
String base64Data = java.util.Base64.getEncoder().encodeToString(binaryData);
System.out.println(base64Data);
```

#### Regenerate Test Binary Files

If you need to regenerate the pre-generated binary files:

```bash
# Run the ProtoBinaryGenerator test
./gradlew test --tests "ProtoBinaryGenerator"

# Files are automatically saved to: src/test/resources/
ls src/test/resources/*.pb
```

**Generator Details:**
- Test class: `src/test/java/buf/gradle/demo/ProtoBinaryGenerator.java`
- Generates 7 proto binary files (.pb format only)
- Includes minimal, sample, and comprehensive transaction examples

### Using Buf CLI (Alternative)

```bash
# Create JSON file
cat > request.json << 'EOF'
{
  "parent": "merchants/M-001",
  "transaction": {
    "id": "TXN-001",
    "amount": 100.00,
    "currency": "USD"
  }
}
EOF

# Convert JSON to proto binary (requires buf CLI)
buf convert -I src/main/proto \
  --type payments.v1.CreateTransactionRequest \
  request.json > request.pb
```

## Proto Request Body Types: When to Use Each

### Request Body Format Options

When sending proto to the API, you have these options:

#### 1. **Binary (.pb file) - RECOMMENDED** ✅

**What it is:**
- Raw proto binary format
- Most efficient
- Smallest payload size
- Direct serialization

**When to use:**
- ✅ Production applications
- ✅ High-performance scenarios
- ✅ Machine-to-machine communication
- ✅ Bandwidth-sensitive environments
- ✅ When you control both client and server

**Content-Type:**
```
application/x-protobuf
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/transactions/create \
  -H "Content-Type: application/x-protobuf" \
  --data-binary @create_transaction_request.pb
```

**Size:** ~176 bytes for full transaction

---

#### 2. **Base64 Encoded - For Text-Based Transmission**

**What it is:**
- Binary data encoded as text
- URL-safe transmission
- Readable in logs/debugging

**When to use:**
- ✅ HTTP GET requests (can't send binary in GET)
- ✅ JSON payloads containing binary
- ✅ Debugging/logging readability
- ✅ When infrastructure strips binary data
- ✅ Email/text channel transmission

**Content-Type:**
```
application/json or text/plain
```

**Example with JSON:**
```bash
# Generate Base64
BASE64=$(base64 -i create_transaction_request.pb)

# Send in JSON
curl -X POST http://localhost:8080/api/transactions/create \
  -H "Content-Type: application/json" \
  -d "{\"data\": \"$BASE64\"}"
```

**Size:** ~296 characters (33% larger than binary)

---

#### 3. **JSON - If Server Supports It**

**What it is:**
- Human-readable format
- Easy to debug
- Larger payload
- Requires JSON↔Proto conversion

**When to use:**
- ✅ Browser testing
- ✅ Manual testing
- ✅ Team collaboration/documentation
- ✅ When client doesn't have proto library
- ⚠️ Only if server provides JSON endpoint

**Content-Type:**
```
application/json
```

**Example:**
```bash
curl -X POST http://localhost:8080/api/transactions/create \
  -H "Content-Type: application/json" \
  -d '{
    "parent": "merchants/M-001",
    "transaction": {
      "id": "TXN-001",
      "amount": 150.00,
      "currency": "USD",
      "cardholder": {
        "cardholder_name": "Alice Smith",
        "email": "alice@example.com"
      }
    }
  }'
```

**Size:** ~250+ characters (much larger)

---

### Comparison Table

| Aspect | Binary (.pb) | Base64 | JSON |
|--------|-----------|--------|------|
| **Size** | 176B | 296B (+68%) | 250B+ (+42%) |
| **Performance** | ⭐⭐⭐ Best | ⭐⭐ Medium | ⭐ Slowest |
| **Readability** | ❌ Binary | ⚠️ Encoded text | ✅ Human-readable |
| **Transmission** | Binary only | Any channel | Any channel |
| **Parsing** | Native proto | Base64 decode + proto | JSON parse + convert |
| **Content-Type** | x-protobuf | json/text | json |
| **Best for** | Production | Mixed channels | Debugging |
| **Recommendation** | **USE THIS** | Use if needed | Fallback only |

---

### Decision Flowchart

```
Are you in production/performance-sensitive?
├─ YES → Use Binary (.pb) with x-protobuf
├─ NO  →
    Can you use binary data in HTTP requests?
    ├─ YES → Use Binary (.pb) with x-protobuf
    ├─ NO  →
        Is Base64 acceptable?
        ├─ YES → Encode to Base64, send in JSON
        ├─ NO  → Use JSON (if server supports)
```

---

### For This Demo Project

**Recommended approach:** Use **Binary (.pb) files**
- Pre-generated .pb files in `src/test/resources/`
- Smallest payload
- Most efficient
- Demonstrates real proto usage

**How to test different formats:**

**1. Binary (Recommended):**
```bash
curl -X POST http://localhost:8080/api/transactions/create \
  -H "Content-Type: application/x-protobuf" \
  --data-binary @src/test/resources/create_transaction_request.pb
```

**2. Base64 (If needed):**
```bash
BASE64=$(base64 -i src/test/resources/create_transaction_request.pb)
curl -X POST http://localhost:8080/api/transactions/create \
  -H "Content-Type: application/json" \
  -d "{\"data\": \"$BASE64\"}"
```

**3. JSON (Debugging only):**
```bash
curl -X POST http://localhost:8080/api/transactions/create \
  -H "Content-Type: application/json" \
  -d '{
    "parent": "merchants/M-001",
    "transaction": {
      "id": "TXN-001",
      "amount": 100.00,
      "currency": "USD"
    }
  }'
```

---

## Testing with Postman

### Setup Postman Environment

1. **Start the application:**
   ```bash
   ./gradlew run
   ```

2. **Open Postman** and create a new request

### Test: Create Transaction (Proto Binary)

**Request Setup:**

| Field | Value |
|-------|-------|
| **Method** | POST |
| **URL** | `http://localhost:8080/api/transactions/create` |
| **Content-Type** | `application/x-protobuf` |
| **Body** | Binary (proto format) |

**Content-Type Options:**

| Type | Pros | Cons | Use Case |
|------|------|------|----------|
| `application/x-protobuf` | ✅ Semantically correct for proto | Requires server support | **Recommended for protobuf** |
| `application/octet-stream` | ✅ Universal support | Generic, not specific | Fallback/compatibility |

We recommend **`application/x-protobuf`** because it explicitly declares the content is Protocol Buffer format.

**Postman Steps:**

1. Click **Body** tab
2. Select **Binary** option
3. Click **Select File** and choose your `.pb` file (or paste Base64)
4. Click **Send**

**Expected Response:**
```
Status: 200 OK
Body: Binary proto response
Headers:
  Content-Type: application/octet-stream
```

### Example: Using Base64 in Postman

1. Generate Base64 encoded proto binary:
   ```bash
   java -cp build/libs/*.jar buf.gradle.demo.Library << 'EOF'
   // Generate and print Base64
   EOF
   ```

2. In Postman **Body** → **raw** → select **JSON**:
   ```json
   {
     "data": "CgIBAAoNTWVyY2hhbnRzL00tMDAxEgsKBFRYTi0wMDERAAAAAAAA5kAT9QRVFkU="
   }
   ```

3. Or use **Binary** mode and paste Base64

### Test: Get Transaction

**Request Setup:**

| Field | Value |
|-------|-------|
| **Method** | POST |
| **URL** | `http://localhost:8080/api/transactions/get` |
| **Content-Type** | `application/x-protobuf` |
| **Body** | Binary proto (GetTransactionRequest) |

**Curl Example:**
```bash
curl -X POST http://localhost:8080/api/transactions/get \
  -H "Content-Type: application/x-protobuf" \
  --data-binary @get_request.pb \
  -o get_response.pb
```

### Test: List Transactions

```bash
curl -X POST http://localhost:8080/api/transactions/list \
  -H "Content-Type: application/x-protobuf" \
  --data-binary @list_request.pb \
  -o list_response.pb
```

### Test: Delete Transaction

```bash
curl -X POST http://localhost:8080/api/transactions/delete \
  -H "Content-Type: application/x-protobuf" \
  --data-binary @delete_request.pb \
  -o delete_response.pb
```

## Understanding the Response

### Proto Response Parsing

**In Postman:**

1. Save binary response: **Save Response** → **Save as File**
2. Decode using Java:
   ```java
   byte[] responseData = Files.readAllBytes(Paths.get("response.pb"));
   CreateTransactionResponse response =
       CreateTransactionResponse.parseFrom(responseData);

   System.out.println("Transaction ID: " +
       response.getTransaction().getId());
   System.out.println("Amount: " +
       response.getTransaction().getAmount());
   System.out.println("Create Time: " +
       response.getTransaction().getCreateTime());
   ```

### Using Base64 for HTTP

**Client sends:**
```bash
echo -n "binary_data" | base64
```

**Server receives:**
```bash
curl -X POST http://localhost:8080/api/transactions/create \
  -H "Content-Type: application/x-protobuf" \
  --data-raw "$(echo -n "binary_data" | base64 -d)"
```

## Complete Example: End-to-End Flow

### 1. Generate Proto Binary (Java)

```java
// File: GenerateTestData.java
import com.example.payments.v1.*;
import java.nio.file.*;
import java.util.Base64;

public class GenerateTestData {
    public static void main(String[] args) throws Exception {
        // Create transaction
        Transaction txn = Transaction.newBuilder()
            .setId("TXN-001")
            .setAmount(150.00)
            .setCurrency("USD")
            .setCardholder(CardHolder.newBuilder()
                .setCardholderName("Alice Smith")
                .setEmail("alice@example.com")
                .setBillingAddress(Address.newBuilder()
                    .setLine1("123 Main St")
                    .setCity("Seattle")
                    .setZipcode("98101")
                    .setCountry("US")
                    .build())
                .build())
            .setStore(Store.newBuilder()
                .setStoreId("STORE-SEA")
                .setStoreName("Seattle Store")
                .setAddress(Address.newBuilder()
                    .setLine1("456 Pine Ave")
                    .setCity("Seattle")
                    .setZipcode("98102")
                    .setCountry("US")
                    .build())
                .build())
            .build();

        // Create request
        CreateTransactionRequest req = CreateTransactionRequest.newBuilder()
            .setParent("merchants/M-001")
            .setTransaction(txn)
            .build();

        // Save binary
        byte[] binary = req.toByteArray();
        Files.write(Paths.get("create_request.pb"), binary);

        // Print Base64 for Postman
        String base64 = Base64.getEncoder().encodeToString(binary);
        System.out.println("Base64 (for Postman):");
        System.out.println(base64);
    }
}
```

**Compile and run:**
```bash
javac -cp build/libs/* GenerateTestData.java
java -cp build/libs/*:. GenerateTestData > base64_data.txt
```

### 2. Test in Postman

1. Copy Base64 from `base64_data.txt`
2. Create Postman **Binary** request body
3. Paste Base64 data
4. Send to `http://localhost:8080/api/transactions/create`

### 3. Parse Response

```bash
# Save Postman response as binary file
# Then parse with Java:
java -cp build/libs/* -c "
  byte[] data = Files.readAllBytes(Paths.get('response.pb'));
  CreateTransactionResponse resp = CreateTransactionResponse.parseFrom(data);
  System.out.println(resp);
"
```

## Troubleshooting

### "Cannot find proto classes"

```bash
# Ensure protos are generated
./gradlew clean generateProto

# Verify classes exist
ls build/generated/sources/proto/main/java/com/example/payments/v1/
```

### "InvalidProtocolBufferException"

```bash
# Verify proto binary format
# Check if data is corrupted or wrong type
od -A x -t x1z -v request.pb | head -20

# Use Java to validate
java -cp build/libs/* -c "
  byte[] data = Files.readAllBytes(Paths.get('request.pb'));
  System.out.println('Size: ' + data.length);
  System.out.println('Hex: ' +
    javax.xml.bind.DatatypeConverter.printHexBinary(data));
"
```

### "Content-Type not recognized"

```bash
# Use correct Content-Type for proto binary
curl -X POST http://localhost:8080/api/transactions/create \
  -H "Content-Type: application/x-protobuf" \
  -H "Accept: application/x-protobuf" \
  --data-binary @request.pb

# Or fallback to generic binary type if needed
curl -X POST http://localhost:8080/api/transactions/create \
  -H "Content-Type: application/octet-stream" \
  -H "Accept: application/octet-stream" \
  --data-binary @request.pb
```

## Further Reading

- **Generated Java Code**: `build/generated/sources/proto/main/java/`
- **Generated API Docs**: `build/generated/sources/proto/main/doc/index.html`
- **Test Suite**: `src/test/java/buf/gradle/demo/LibraryTest.java`
- **Proto Definitions**: `src/main/proto/payments/v1/`
- **Buf Documentation**: https://buf.build/docs
- **Protocol Buffers**: https://protobuf.dev

## Quick Reference

```bash
# Build everything
./gradlew clean build

# Run tests
./gradlew test

# Generate docs
./gradlew generateProto

# View generated code
cat build/generated/sources/proto/main/java/com/example/payments/v1/Transaction.java

# View API docs
open build/generated/sources/proto/main/doc/index.html

# Run application
./gradlew run

# Test with curl
curl -X POST http://localhost:8080/api/transactions/create \
  -H "Content-Type: application/octet-stream" \
  --data-binary @request.pb \
  -o response.pb
```
