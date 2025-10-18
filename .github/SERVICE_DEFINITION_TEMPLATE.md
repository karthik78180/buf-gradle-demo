# Service Definition Pattern - Company Standard

This is the standard pattern for organizing Protocol Buffer definitions across all company repositories.

## Philosophy

**Separation of Concerns:**
- **Messages files** (`{domain}/v1/{domain}.proto`) - Data structures, fields, constraints, enums, types (versioned by domain)
- **Service file** (`schema/service/v1/service.proto`) - ALL gRPC services, REST bindings, metadata (centralized)
- **Metadata file** (`schema/meta/v1/meta.proto`) - Shared custom metadata extensions

This separation allows:
- Messages to be versioned independently per domain
- Services to be managed centrally
- Easy reuse of message types across multiple services

## Directory Structure

```
src/main/proto/
├── {domain}/
│   └── v1/
│       └── {domain}.proto              # Messages, enums, types only
└── schema/
    ├── service/v1/
    │   └── service.proto               # ALL services (centralized)
    └── meta/v1/
        └── meta.proto                  # Shared metadata extensions
```

## File Organization Rules

### Rule 1: Messages File (`{domain}.proto`)

**Purpose:** Define data structures only

**Contains:**
- Message definitions
- Enums
- Field constraints (REQUIRED, OUTPUT_ONLY)

**Does NOT contain:**
- Service definitions
- RPC method definitions
- google.api.http bindings
- Metadata options

**Imports:**
```proto
import "google/api/field_behavior.proto";
import "google/protobuf/timestamp.proto";
// NO google.api.annotations and NO metadata imports
```

### Rule 2: Service File (`schema/service.proto`)

**Purpose:** Define ALL gRPC services and REST APIs (centralized)

**Contains:**
- Service definitions (interfaces) for all domains
- RPC methods
- google.api.http bindings (REST paths)
- Service-level metadata (via service_meta)

**Does NOT contain:**
- Message definitions (import them instead)
- Duplicate message definitions

**Imports:**
```proto
import "google/api/annotations.proto";
import "payments/v1/transaction.proto";    # Import from domain packages
import "orders/v1/order.proto";            # Multiple domains can be here
import "schema/meta/v1/meta.proto";
```

### Rule 3: Metadata File (`schema/meta/v1/meta.proto`)

**Purpose:** Shared service metadata extension

**Contains:**
- `service_meta` extension for RPC services (ServiceOptions)
- `Metadata` message with tags map for service classification

**Location:** `src/main/proto/schema/meta/v1/meta.proto` (created once, reused in service file only)

## Example: Payment Domain

### File 1: `src/main/proto/payments/v1/transaction.proto` (Messages only)

```proto
syntax = "proto3";

package payments.v1;

import "google/api/field_behavior.proto";
import "google/protobuf/timestamp.proto";

option java_multiple_files = true;
option java_package = "com.example.payments.v1";

// Message definitions ONLY - no services or metadata here!

message Transaction {
  string id = 1 [(google.api.field_behavior) = REQUIRED];
  double amount = 2 [(google.api.field_behavior) = REQUIRED];
  string currency = 3 [(google.api.field_behavior) = REQUIRED];
  google.protobuf.Timestamp create_time = 4 [(google.api.field_behavior) = OUTPUT_ONLY];
}

message CreateTransactionRequest {
  Transaction transaction = 1 [(google.api.field_behavior) = REQUIRED];
}

message CreateTransactionResponse {
  Transaction transaction = 1;
}

message GetTransactionRequest {
  string id = 1 [(google.api.field_behavior) = REQUIRED];
}

message GetTransactionResponse {
  Transaction transaction = 1;
}

message ListTransactionsRequest {
  int32 page_size = 1;
  string page_token = 2;
}

message ListTransactionsResponse {
  repeated Transaction transactions = 1;
  string next_page_token = 2;
}

message UpdateTransactionRequest {
  string id = 1 [(google.api.field_behavior) = REQUIRED];
  Transaction transaction = 2 [(google.api.field_behavior) = REQUIRED];
}

message UpdateTransactionResponse {
  Transaction transaction = 1;
}

message DeleteTransactionRequest {
  string id = 1 [(google.api.field_behavior) = REQUIRED];
}

message DeleteTransactionResponse {}
```

### File 2: `src/main/proto/schema/service/v1/service.proto` (All services centralized)

```proto
syntax = "proto3";

// Centralized service definitions for all domains
package schema.service.v1;

import "google/api/annotations.proto";
import "schema/meta/v1/meta.proto";
import "payments/v1/transaction.proto";

option java_multiple_files = true;
option java_package = "com.example.service.v1";

// TransactionService - Payment transaction operations
service TransactionService {
  // Service-level metadata
  option (schema.meta.v1.service_meta) = {
    tags: { key: "x-service-tier" value: "core" }
    tags: { key: "x-domain" value: "payments" }
  };

  // Create a new transaction
  rpc CreateTransaction(payments.v1.CreateTransactionRequest) returns (payments.v1.CreateTransactionResponse) {
    option (google.api.http) = {
      post: "/v1/transactions"
      body: "transaction"
    };
  }

  // Get a transaction by ID
  rpc GetTransaction(payments.v1.GetTransactionRequest) returns (payments.v1.GetTransactionResponse) {
    option (google.api.http) = {
      get: "/v1/transactions/{name}"
    };
  }

  // List transactions with pagination
  rpc ListTransactions(payments.v1.ListTransactionsRequest) returns (payments.v1.ListTransactionsResponse) {
    option (google.api.http) = {
      get: "/v1/transactions"
    };
  }

  // Update a transaction
  rpc UpdateTransaction(payments.v1.UpdateTransactionRequest) returns (payments.v1.UpdateTransactionResponse) {
    option (google.api.http) = {
      patch: "/v1/transactions/{name}"
      body: "*"
    };
  }

  // Delete a transaction
  rpc DeleteTransaction(payments.v1.DeleteTransactionRequest) returns (payments.v1.DeleteTransactionResponse) {
    option (google.api.http) = {
      delete: "/v1/transactions/{name}"
    };
  }
}

// Add more services for other domains in this same file:
// service OrderService { ... }
// service UserService { ... }
```

## Standard REST Paths

**Pattern:** `/v1/{resource_plural}/{id?}`

```
Create:  POST   /v1/{resource_plural}
Get:     GET    /v1/{resource_plural}/{id}
List:    GET    /v1/{resource_plural}
Update:  PATCH  /v1/{resource_plural}/{id}
Delete:  DELETE /v1/{resource_plural}/{id}
```

## Project Configuration

Project metadata is stored in `.github/project-config.yaml` as the **single source of truth**.

**Update this file once per project:**

```yaml
projectId: my-project
authenticationType: JWT
documentationUrl: https://example.com/my-project/
projectTitle: My API Service Suite
```

These values are displayed in the generated documentation **before the Table of Contents** in a styled info box, providing quick reference for API consumers.

### How It Works

1. **Single Source of Truth:** `.github/project-config.yaml` contains all project metadata
2. **Template Reference:** `proto-doc.tmpl` reads from this config (indicated by the comment: `<!-- Source: .github/project-config.yaml -->`)
3. **No Duplication:** Update the config file once, and it's reflected in all generated documentation

### For New Projects

Copy `.github/project-config.yaml` and update with your project's details:
- `projectId`: Your project identifier
- `authenticationType`: Auth method (JWT, OAUTH2, API_KEY, etc.)
- `documentationUrl`: GitHub Pages or hosted documentation URL
- `projectTitle`: Display name for your API

## Metadata Levels

### Service-level Metadata (`service_meta`)

File-level metadata is attached to the service proto file:

```proto
// File-level metadata
option (schema.meta.v1.file_meta) = {
  project_id: "my-project"
  authentication_type: "JWT"
  gh_page_url: "https://example.com/my-project/"
};
```

**Fields:**
- `project_id`: Unique identifier for the project
- `authentication_type`: Authentication mechanism (JWT, OAUTH2, API_KEY, etc.)
- `gh_page_url`: GitHub Pages URL for published documentation

### Service-level Metadata (`service_meta`)

Service-level metadata is attached to service definitions:

```proto
service TransactionService {
  option (schema.meta.v1.service_meta) = {
    tags: { key: "x-service-tier" value: "core" }
    tags: { key: "x-domain" value: "payments" }
  };

  // RPC methods...
}
```

**Common tags:**
- `x-service-tier`: core, standard, experimental
- `x-domain`: business domain (payments, orders, etc.)
- `x-api-version`: v1, v2, etc.

## Usage Across Company Repos

1. **Copy this template** - Use `.github/SERVICE_DEFINITION_TEMPLATE.md`
2. **Follow the structure**:
   - Messages: `{domain}/v1/{domain}.proto` (versioned per domain)
   - Services: `schema/service/v1/service.proto` (centralized, one file for all)
   - Metadata: `schema/meta/v1/meta.proto` (shared)
3. **Reuse metadata** - Import `schema/meta/v1/meta.proto` in service file
4. **Standard paths** - Use `/v1/{resource}` pattern for REST
5. **Centralized approach** - Add all domain services to single `schema/service/v1/service.proto` file

## Benefits

✅ **Separation of Concerns** - Messages versioned per domain, services centralized
✅ **Versioning** - Message versions evolve independently per domain
✅ **Reusability** - Message types can be imported by multiple services
✅ **Centralized API** - All services defined in one place (schema/service/v1/service.proto)
✅ **Easy Management** - Add new services without creating new files
✅ **Consistency** - Standard pattern across all company repos
✅ **Simple & Minimal** - Metadata on services only, clean message files
✅ **Documentation** - Services rendered prominently in generated docs
