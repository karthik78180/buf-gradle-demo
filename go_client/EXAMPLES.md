# Go Client Usage Examples

Detailed examples for each CRUD operation.

## Table of Contents

1. [CREATE - Create Transaction](#create)
2. [READ - Get Transaction](#read)
3. [LIST - List Transactions](#list)
4. [DELETE - Delete Transaction](#delete)

---

## CREATE - Create Transaction {#create}

**Operation:** Create a new payment transaction

### Code Example

```go
// Create a new transaction with full details
transaction := &pb.Transaction{
    Id:       "TXN-001",
    Amount:   150.00,
    Currency: "USD",
    Cardholder: &pb.CardHolder{
        CardholderName: "Alice Smith",
        Email:          "alice@example.com",
        BillingAddress: &pb.Address{
            Line1:    "123 Main St",
            Line2:    "Apt 5",
            City:     "Seattle",
            State:    "WA",
            Zipcode:  "98101",
            Country:  "US",
        },
    },
    Store: &pb.Store{
        StoreId:   "STORE-001",
        StoreName: "Seattle Downtown Store",
        Address: &pb.Address{
            Line1:    "456 Pine Ave",
            City:     "Seattle",
            State:    "WA",
            Zipcode:  "98102",
            Country:  "US",
        },
    },
}

client := NewPaymentClient("http://localhost:8080")
response, err := client.CreateTransaction(transaction)
if err != nil {
    log.Fatalf("Create failed: %v", err)
}

// Access response
fmt.Printf("Created: %s\n", response.Transaction.Id)
fmt.Printf("Create Time: %v\n", response.Transaction.CreateTime)
fmt.Printf("Update Time: %v\n", response.Transaction.UpdateTime)
```

### Proto Message

```protobuf
message CreateTransactionRequest {
  string parent = 1;
  Transaction transaction = 2 [(google.api.field_behavior) = REQUIRED];
}

message CreateTransactionResponse {
  Transaction transaction = 1;
}
```

### Request/Response Size

- **Request:** ~176 bytes (binary proto)
- **Response:** ~200+ bytes (includes timestamps)
- **Efficiency:** Compact binary format

### Go Run Example

```bash
# Run with custom transaction
go run main.go
# Output:
# === CREATE TRANSACTION ===
# Request size: 176 bytes
# Transaction ID: TXN-001
# Amount: $150.00 USD
# ✅ Transaction created successfully
#    Create Time: 2024-10-20T...Z
#    Update Time: 2024-10-20T...Z
```

---

## READ - Get Transaction {#read}

**Operation:** Retrieve a transaction by ID

### Code Example

```go
client := NewPaymentClient("http://localhost:8080")

// Get transaction by ID
response, err := client.GetTransaction("TXN-001")
if err != nil {
    log.Fatalf("Get failed: %v", err)
}

// Access response
if response.Transaction != nil {
    txn := response.Transaction
    fmt.Printf("ID: %s\n", txn.Id)
    fmt.Printf("Amount: $%.2f %s\n", txn.Amount, txn.Currency)
    fmt.Printf("Cardholder: %s\n", txn.Cardholder.CardholderName)
    fmt.Printf("Store: %s\n", txn.Store.StoreName)
}
```

### Proto Message

```protobuf
message GetTransactionRequest {
  string name = 1 [(google.api.field_behavior) = REQUIRED];
}

message GetTransactionResponse {
  Transaction transaction = 1;
}
```

### Request/Response Size

- **Request:** ~22 bytes (minimal - just ID)
- **Response:** ~150+ bytes (full transaction)
- **Efficiency:** Very efficient request

### Go Run Example

```bash
# Will automatically fetch after create
go run main.go
# Output:
# === GET TRANSACTION ===
# Request size: 22 bytes
# Looking up: transactions/TXN-001
# ✅ Transaction retrieved successfully
#    ID: TXN-001
#    Amount: $150.00 USD
```

### Advanced: Error Handling

```go
response, err := client.GetTransaction("NONEXISTENT")
if err != nil {
    if strings.Contains(err.Error(), "404") {
        fmt.Println("Transaction not found")
    } else if strings.Contains(err.Error(), "connection refused") {
        fmt.Println("Server not running")
    } else {
        fmt.Printf("Error: %v\n", err)
    }
}
```

---

## LIST - List Transactions {#list}

**Operation:** Retrieve multiple transactions with pagination

### Code Example

```go
client := NewPaymentClient("http://localhost:8080")

// List transactions with pagination
response, err := client.ListTransactions(10) // pageSize: 10
if err != nil {
    log.Fatalf("List failed: %v", err)
}

// Process results
fmt.Printf("Total transactions: %d\n", len(response.Transactions))

for i, txn := range response.Transactions {
    fmt.Printf("[%d] ID: %s, Amount: $%.2f %s\n",
        i+1, txn.Id, txn.Amount, txn.Currency)
}

// Check for next page
if response.NextPageToken != "" {
    fmt.Printf("Next page token: %s\n", response.NextPageToken)
}
```

### Proto Message

```protobuf
message ListTransactionsRequest {
  string parent = 1;
  int32 page_size = 2;
  string page_token = 3;
}

message ListTransactionsResponse {
  repeated Transaction transactions = 1;
  string next_page_token = 2;
}
```

### Request/Response Size

- **Request:** ~20 bytes (metadata only)
- **Response:** ~300+ bytes (multiple transactions)
- **Efficiency:** Efficient pagination

### Go Run Example

```bash
# Will automatically list after create
go run main.go
# Output:
# === LIST TRANSACTIONS ===
# Request size: 20 bytes
# Page size: 10
# ✅ Transactions listed successfully
#    Total transactions: 2
#    [1] ID: TXN-001, Amount: $150.00 USD
#    [2] ID: TXN-002, Amount: $99.99 USD
```

### Advanced: Pagination Loop

```go
pageToken := ""

for {
    req := &pb.ListTransactionsRequest{
        Parent:   "accounts/ACC-001",
        PageSize: 10,
        PageToken: pageToken,
    }

    response, err := client.ListTransactions(req.PageSize)
    if err != nil {
        break
    }

    // Process transactions
    for _, txn := range response.Transactions {
        fmt.Printf("- %s: $%.2f\n", txn.Id, txn.Amount)
    }

    // Check for next page
    if response.NextPageToken == "" {
        break
    }
    pageToken = response.NextPageToken
}
```

---

## DELETE - Delete Transaction {#delete}

**Operation:** Delete a transaction by ID

### Code Example

```go
client := NewPaymentClient("http://localhost:8080")

// Delete transaction by ID
response, err := client.DeleteTransaction("TXN-001")
if err != nil {
    log.Fatalf("Delete failed: %v", err)
}

// Response is empty (successful deletion only)
fmt.Println("Transaction deleted successfully")
```

### Proto Message

```protobuf
message DeleteTransactionRequest {
  string name = 1 [(google.api.field_behavior) = REQUIRED];
}

message DeleteTransactionResponse {
  // Empty response - only status matters
}
```

### Request/Response Size

- **Request:** ~22 bytes (just ID)
- **Response:** ~0 bytes (empty, only HTTP status)
- **Efficiency:** Minimal overhead

### Go Run Example

```bash
# Will automatically delete after create
go run main.go
# Output:
# === DELETE TRANSACTION ===
# Request size: 22 bytes
# Deleting: transactions/TXN-001
# ✅ Transaction deleted successfully
```

### Advanced: Verify Deletion

```go
// Delete
_, err := client.DeleteTransaction("TXN-001")
if err != nil {
    log.Printf("Delete error: %v", err)
    return
}

// Verify it's gone
response, err := client.GetTransaction("TXN-001")
if err != nil && strings.Contains(err.Error(), "not found") {
    fmt.Println("✅ Deletion verified - transaction not found")
} else if response.Transaction == nil {
    fmt.Println("✅ Deletion verified - empty response")
}
```

---

## Complete CRUD Workflow

### Full Example

```go
package main

import (
    "fmt"
    "log"
    pb "go.example.com/payments-client/gen/payments/v1"
)

func main() {
    client := NewPaymentClient("http://localhost:8080")

    // 1. CREATE
    fmt.Println("Step 1: Creating transaction...")
    createResp, err := client.CreateTransaction(&pb.Transaction{
        Id:       "DEMO-001",
        Amount:   99.99,
        Currency: "USD",
        // ... full transaction details
    })
    if err != nil {
        log.Fatalf("Create failed: %v", err)
    }
    txnID := createResp.Transaction.Id

    // 2. READ
    fmt.Println("Step 2: Reading transaction...")
    readResp, err := client.GetTransaction(txnID)
    if err != nil {
        log.Fatalf("Read failed: %v", err)
    }
    fmt.Printf("Read: %s ($%.2f)\n", readResp.Transaction.Id, readResp.Transaction.Amount)

    // 3. LIST
    fmt.Println("Step 3: Listing all transactions...")
    listResp, err := client.ListTransactions(10)
    if err != nil {
        log.Fatalf("List failed: %v", err)
    }
    fmt.Printf("Found %d transactions\n", len(listResp.Transactions))

    // 4. DELETE
    fmt.Println("Step 4: Deleting transaction...")
    _, err = client.DeleteTransaction(txnID)
    if err != nil {
        log.Fatalf("Delete failed: %v", err)
    }

    fmt.Println("✅ Full CRUD workflow completed!")
}
```

---

## Binary Proto Format Details

### Message Size Breakdown

**CreateTransactionRequest (~176B):**
- Transaction ID: ~5B
- Amount (double): 8B
- Currency: ~3B
- Cardholder: ~50B
- Store: ~80B
- Other fields: ~25B

**Why Binary?**
```
Text (JSON):     350+ bytes (readable but large)
Binary (Proto):  176 bytes  (compact and fast)
Compression:     33% smaller
Parsing:         10x faster
Network:         Less bandwidth
```

---

## Tips & Best Practices

### 1. Always Check Errors

```go
response, err := client.CreateTransaction(txn)
if err != nil {
    // Log, retry, or fallback
    log.Printf("Error: %v", err)
}
```

### 2. Reuse Client

```go
// Good: Create once, reuse many times
client := NewPaymentClient("http://localhost:8080")
for i := 0; i < 100; i++ {
    client.CreateTransaction(txn)
}

// Bad: Don't create new client each time
for i := 0; i < 100; i++ {
    NewPaymentClient().CreateTransaction(txn) // Inefficient
}
```

### 3. Validate Required Fields

```go
txn := &pb.Transaction{
    Id:       "TXN-001",        // REQUIRED
    Amount:   150.00,           // REQUIRED
    Currency: "USD",            // REQUIRED
    Cardholder: {...},          // REQUIRED
    Store:    {...},            // REQUIRED
}
```

### 4. Handle Timeouts

```go
client := &http.Client{
    Timeout: 5 * time.Second,
}
paymentClient := &PaymentClient{
    client: client,
}
```

---

For more details, see [README.md](README.md) and [QUICKSTART.md](QUICKSTART.md).
