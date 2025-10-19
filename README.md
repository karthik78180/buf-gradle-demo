# Buf Gradle Demo: Payment Transaction API

A demonstration of a production-ready Protocol Buffer (Proto) setup integrating **Buf**, **Gradle**, and **Java 21** for building scalable microservices APIs. This project showcases best practices for proto-driven API development with automated linting, formatting, and code generation.

## 🎯 Project Overview

This project implements a **Payment Transaction Processing API** using Protocol Buffers with:
- ✅ **Buf** for proto validation, linting, and format enforcement
- ✅ **Gradle** with `protobuf-gradle-plugin` for Java code generation
- ✅ **protoc-gen-doc** for automatic HTML API documentation
- ✅ **gRPC service definitions** with clean RPC method signatures
- ✅ **Comprehensive inline comments** with field behavior constraints and timestamps

## 🏗️ Architecture

### Hybrid Buf + Gradle Setup

```
┌─────────────────────────────────────────────────────────────┐
│                    Build Pipeline                            │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  bufFormatCheck ──→ bufLint ──→ generateProto ──→ compileJava
│  (Buf Format)    (Lint Rules)  (Proto→Java)    (Java Build)
│  (Enforce style) (DEFAULT rules) (protobuf-    (Compile)
│                                   gradle-plugin)
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

| Component | Tool | Version |
|-----------|------|---------|
| **Language** | Java | 21 (LTS) |
| **Build System** | Gradle | 9.0.0+ |
| **Proto Validation & Linting** | Buf | 1.53.0 |
| **Proto Compilation** | protobuf-gradle-plugin | 0.9.5 |
| **Protoc Compiler** | Protocol Buffers | 4.32.1 |
| **Documentation Generation** | protoc-gen-doc | 1.5.1 |
| **Google APIs** | proto-google-common-protos | 2.61.2 |
| **Testing** | JUnit 5 | 5.10.3 |

## 📁 Project Structure

```
buf-gradle-demo/
├── README.md                              # This file
├── .github/
│   ├── copilot-instructions.md           # AI assistant guidelines
│   └── SERVICE_DEFINITION_TEMPLATE.md    # Company-wide service pattern docs
├── build.gradle                           # Gradle build configuration
├── buf.yaml                               # Buf linting & module config
├── gradle.properties                      # Gradle properties
├── settings.gradle                        # Gradle multi-project settings
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/
│   │   │       └── app/
│   │   │           └── App.java          # Demo application entry point
│   │   ├── proto/
│   │   │   ├── payments/v1/
│   │   │   │   ├── transaction.proto     # Transaction request/response messages
│   │   │   │   └── types.proto           # Shared payment types (Address, CardHolder, Store, etc.)
│   │   │   └── schema/service/v1/
│   │   │       └── service.proto         # Centralized service definitions
│   │   └── resources/                    # (empty, for resources)
│   └── test/java/                        # Test files
└── build/
    ├── generated/
    │   ├── sources/proto/main/java/      # Generated Java code
    │   └── sources/proto/main/doc/       # Generated HTML documentation
    ├── bufbuild/                         # Buf build artifacts
    └── extracted-include-protos/         # Extracted proto dependencies
```

## 🚀 Quick Start

### Prerequisites

- **Java 21+** installed
- **Gradle 9.0.0+** (included via wrapper)
- **Git** for version control

### Build the Project

```bash
# Clean build with linting and code generation
./gradlew clean build

# Run only linting (Buf)
./gradlew bufLint

# Apply Buf formatting
./gradlew bufFormatApply

# Run tests
./gradlew test

# View generated code
ls build/generated/sources/proto/main/java/com/example/payments/v1/
```

### Build Task Execution Flow

```bash
./gradlew build
```

This runs:
1. ✅ `bufFormatCheck` - Validates proto formatting consistency
2. ✅ `bufLint` - Applies Buf linting rules (DEFAULT rule set)
3. ✅ `bufValidate` - Comprehensive validation (format + lint)
4. ✅ `generateProto` - Generates Java code + HTML documentation from protos
5. ✅ `compileJava` - Compiles Java source code
6. ✅ `test` - Runs tests
7. ✅ `assemble` - Creates JAR artifact

## 📝 Core Proto Definitions

### Proto Organization Pattern

Following company-wide best practices, the project splits proto definitions into logical concerns:

#### 1. Shared Types (`payments/v1/types.proto`)

Common domain types used across services:

```proto
syntax = "proto3";
package payments.v1;

import "google/api/field_behavior.proto";

// Reusable address message for billing/shipping
message Address {
  string line1 = 1 [(google.api.field_behavior) = REQUIRED];
  string line2 = 2;
  string city = 3 [(google.api.field_behavior) = REQUIRED];
  string state = 4;
  string zipcode = 5 [(google.api.field_behavior) = REQUIRED];
  string country = 6 [(google.api.field_behavior) = REQUIRED];
}

// Card holder information
message CardHolder {
  string cardholder_name = 1 [(google.api.field_behavior) = REQUIRED];
  string email = 2;
  Address billing_address = 3;
}

// Store/merchant location information
message Store {
  string store_id = 1 [(google.api.field_behavior) = REQUIRED];
  string store_name = 2;
  Address address = 3;
}
```

#### 2. Transaction Messages (`payments/v1/transaction.proto`)

Transaction-specific request/response and core messages:

```proto
syntax = "proto3";
package payments.v1;

import "google/api/field_behavior.proto";
import "google/protobuf/timestamp.proto";
import "payments/v1/types.proto";

// Core transaction message
message Transaction {
  string id = 1 [(google.api.field_behavior) = REQUIRED];
  string client_token = 2;  // Idempotency key
  double amount = 3 [(google.api.field_behavior) = REQUIRED];
  string currency = 4 [(google.api.field_behavior) = REQUIRED];
  CardHolder cardholder = 5 [(google.api.field_behavior) = REQUIRED];
  Store store = 6 [(google.api.field_behavior) = REQUIRED];
  ProductDetails products = 7;
  google.protobuf.Timestamp create_time = 8 [(google.api.field_behavior) = OUTPUT_ONLY];
  google.protobuf.Timestamp update_time = 9 [(google.api.field_behavior) = OUTPUT_ONLY];
}

// CRUD Request/Response pairs
message CreateTransactionRequest {
  string parent = 1;
  Transaction transaction = 2 [(google.api.field_behavior) = REQUIRED];
}

message CreateTransactionResponse {
  Transaction transaction = 1;
}
// ... GetTransactionRequest/Response, UpdateTransactionRequest/Response, etc.
```

#### 3. Service Definitions (`schema/service/v1/service.proto`)

Centralized gRPC service definitions for all payment operations:

```proto
syntax = "proto3";
package schema.service.v1;

import "payments/v1/transaction.proto";

service TransactionService {
  // Create a new payment transaction
  rpc CreateTransaction(payments.v1.CreateTransactionRequest) returns (payments.v1.CreateTransactionResponse) {}

  // Retrieve a transaction by ID
  rpc GetTransaction(payments.v1.GetTransactionRequest) returns (payments.v1.GetTransactionResponse) {}

  // List transactions with pagination
  rpc ListTransactions(payments.v1.ListTransactionsRequest) returns (payments.v1.ListTransactionsResponse) {}

  // Update transaction details
  rpc UpdateTransaction(payments.v1.UpdateTransactionRequest) returns (payments.v1.UpdateTransactionResponse) {}

  // Delete a transaction
  rpc DeleteTransaction(payments.v1.DeleteTransactionRequest) returns (payments.v1.DeleteTransactionResponse) {}
}
```

### Key Design Features

- ✅ **Separation of Concerns**: Shared types (types.proto), domain messages (transaction.proto), service definitions (service.proto)
- ✅ **Field Behavior Constraints**: `REQUIRED` and `OUTPUT_ONLY` to enforce API contracts
- ✅ **Timestamps**: Server-generated `create_time` and `update_time` for audit trails
- ✅ **Idempotency Keys**: `client_token` for safe transaction retries
- ✅ **Clean RPC Methods**: Pure gRPC signatures without HTTP bindings

## 🛡️ Buf: Proto Validation & Linting

### What is Buf?

**Buf** is a modern protocol buffer toolchain that provides:
- **Linting**: Enforces style, naming, and documentation standards
- **Formatting**: Automatic proto file formatting (consistent indentation, spacing)
- **Breaking Change Detection**: Identifies incompatible API modifications
- **Module Management**: Dependency resolution for proto imports
- **Code Generation**: (disabled in this project - we use protobuf-gradle-plugin instead)

### Why We Use Buf

| Need | Solution |
|------|----------|
| **Consistency** | Enforce naming conventions, package versioning, import patterns |
| **Documentation** | Require comments on all messages, fields, services, RPCs |
| **Breaking Changes** | Detect when proto modifications break existing clients |
| **Quality Gates** | Fail builds automatically on lint violations |
| **Team Standards** | Share linting rules across company repositories |

### What Does Buf Expose?

Buf exposes these validation layers through the Gradle `build.buf` plugin:

1. **bufLint** - Proto linting with DEFAULT rule set
   - Naming conventions (package names, message names, field names)
   - Requires documentation comments
   - Detects unused imports
   - Validates enum values
   - Enforces proto3 syntax

2. **bufFormatCheck** - Format validation
   - Consistent indentation (2 spaces)
   - Line length limits
   - Proper spacing around braces and separators
   - Line ending consistency

3. **bufFormatApply** - Automatic format fixing
   - Auto-corrects formatting violations
   - Maintains code semantics
   - Safe to run on all proto files

4. **bufValidate** - Custom validation task (combining linting + format checks)
   - Runs both `bufLint` and `bufFormatCheck`
   - Single comprehensive validation step
   - Integrated into build pipeline

### How to Use Buf in This Project

**Run linting only:**
```bash
./gradlew bufLint
```

**Check formatting without fixing:**
```bash
./gradlew bufFormatCheck
```

**Auto-fix formatting violations:**
```bash
./gradlew bufFormatApply
```

**Comprehensive validation (both lint + format):**
```bash
./gradlew bufValidate
```

**Full build with validation:**
```bash
./gradlew clean build  # Runs bufValidate automatically
```

### Example Buf Rules Enforced

The DEFAULT rule set includes:

```yaml
# Package naming: lowercase with dots (package.v1, package.v2beta1)
PACKAGE_VERSION_SUFFIX

# Message naming: PascalCase
MESSAGE_PASCAL_CASE

# Field naming: snake_case
FIELD_LOWER_SNAKE_CASE

# RPC naming: PascalCase
RPC_PASCAL_CASE

# Require comments on public types
COMMENT_ENUM
COMMENT_MESSAGE
COMMENT_FIELD
COMMENT_RPC

# Detect unused imports
UNLINKED_TYPE_REFERENCE
```

See all rules: https://buf.build/docs/lint/rules

---

## 📚 protoc-gen-doc: API Documentation Generation

### What is protoc-gen-doc?

**protoc-gen-doc** is a Protocol Buffer compiler plugin that generates **beautiful HTML/Markdown documentation** directly from your `.proto` files and comments.

It works by:
1. Parsing your proto definitions
2. Extracting inline comments and documentation
3. Rendering HTML/Markdown output with full API reference
4. Organizing by files, packages, messages, services, and enums

### What Does protoc-gen-doc Expose?

The generated HTML documentation includes:

1. **Table of Contents**
   - All proto files organized hierarchically
   - Services marked with "S" badge
   - Messages marked with "M" badge
   - Enums marked with "E" badge

2. **Service Documentation**
   - RPC method signatures
   - Request/Response type links
   - Method descriptions from comments

3. **Message Documentation**
   - Message field definitions
   - Field types and labels (optional, repeated, etc.)
   - Field descriptions and constraints
   - Default values

4. **Type References**
   - Cross-linked message and enum definitions
   - Scalar value type reference table
   - Full type hierarchy

5. **Search & Navigation**
   - Inline anchors for each definition
   - Deep-linkable sections
   - Index of all definitions

### What Solution Does It Provide?

| Problem | Solution |
|---------|----------|
| **API Discovery** | Complete API reference auto-generated from code |
| **Always In Sync** | Changes in protos automatically update docs |
| **No Manual Docs** | Comments in protos become the API reference |
| **Version Tracking** | Separate docs for each proto version |
| **Client Onboarding** | Clear examples of request/response structures |

### How to Use protoc-gen-doc

**Generate documentation (automatic during build):**
```bash
./gradlew generateProto
# or
./gradlew clean build
```

**View generated documentation:**
```bash
open build/generated/sources/proto/main/doc/index.html
```

**Documentation location:**
```
build/generated/sources/proto/main/doc/
├── index.html          # Main documentation entry point
├── stylesheet.css      # (if provided)
└── (other assets)
```

### Example Generated Documentation Structure

For our Payment Transaction API, the docs will include:

```
SERVICES (rendered first)
├── schema.service.v1.TransactionService
│   ├── CreateTransaction
│   ├── GetTransaction
│   ├── ListTransactions
│   ├── UpdateTransaction
│   └── DeleteTransaction

MESSAGES
├── payments.v1.Transaction
├── payments.v1.Address
├── payments.v1.CardHolder
├── payments.v1.Store
├── payments.v1.MerchandiseItem
├── payments.v1.ProductDetails
├── payments.v1.CreateTransactionRequest
├── payments.v1.CreateTransactionResponse
├── ... (other request/response types)

SCALAR VALUE TYPES REFERENCE
├── double → java.lang.Double
├── string → java.lang.String
├── int32 → int
└── ... (all proto scalar types)
```

### How Documentation Is Generated

**In build.gradle:**
```gradle
protobuf {
  plugins {
    doc {
      artifact = 'io.github.pseudomuto:protoc-gen-doc:1.5.1'
    }
  }

  generateProtoTasks {
    all().each { task ->
      task.plugins {
        doc {
          // Uses default built-in template
        }
      }
    }
  }
}
```

**During build:**
1. Protoc compiler processes all `.proto` files
2. protoc-gen-doc plugin extracts comments and metadata
3. Default HTML template generates `index.html`
4. Output placed in `build/generated/sources/proto/main/doc/`

### Customizing Documentation

protoc-gen-doc supports custom templates using Go text templating:
- Create `proto-doc-base.tmpl` with custom layout
- Reference it in build.gradle: `option "your-template.tmpl,index.html"`
- Replace placeholders, customize styling, adjust section ordering

(This project uses the default template for simplicity and maintainability)

---

## ⚙️ Configuration Details

### buf.yaml - Proto Linting & Module Management

```yaml
version: v2
modules:
  - path: src/main/proto
deps:
  - buf.build/googleapis/googleapis  # Includes google.api.* protos

lint:
  use:
    - DEFAULT                         # Use all DEFAULT linting rules

breaking:
  use:
    - FILE                           # Detect file-level breaking changes
```

### build.gradle - Gradle & Protobuf Configuration

Key sections:

```gradle
plugins {
  id 'java'
  id 'application'
  id 'com.google.protobuf' version '0.9.5'
  id 'build.buf' version '0.10.3'
}

dependencies {
  implementation 'com.google.protobuf:protobuf-java:4.32.1'
  implementation 'com.google.api.grpc:proto-google-common-protos:2.61.2'
}

protobuf {
  protoc {
    artifact = 'com.google.protobuf:protoc:4.32.1'
  }
}

sourceSets {
  main {
    proto {
      srcDir 'src/main/proto'
    }
    java {
      srcDirs 'build/generated/sources/proto/main/java'
    }
  }
}

buf {
  enforceFormat = true
  toolVersion = '1.53.0'
  configFileLocation = file('buf.yaml')
}

// Buf linting runs before code generation
tasks.named('bufGenerate').configure { enabled = false }
tasks.named('generateProto') { dependsOn tasks.named('bufLint') }
```

## 📊 Generated Code

After building, Java code is generated in `build/generated/sources/proto/main/java/`:

```
com/example/payments/v1/
├── Transaction.java & TransactionOrBuilder.java
├── Address.java & AddressOrBuilder.java
├── CardHolder.java & CardHolderOrBuilder.java
├── Store.java & StoreOrBuilder.java
├── MerchandiseItem.java & MerchandiseItemOrBuilder.java
├── CreateTransactionRequest.java & ...
├── GetTransactionRequest.java & ...
├── UpdateTransactionRequest.java & ...
├── DeleteTransactionRequest.java & ...
├── PaymentsServiceGrpc.java (if gRPC enabled)
└── PaymentsProto.java (descriptor holder)

schema/meta/v1/
├── Metadata.java & MetadataOrBuilder.java
└── Meta.java (descriptor holder)
```

## 🔍 Linting Rules

The project uses **Buf DEFAULT rules**:

| Category | Rules Enforced |
|----------|-----------------|
| **Style** | Package versioning, naming conventions |
| **Comments** | Required comments on messages, fields, RPCs |
| **Imports** | Unused imports detection, cyclic dependency checks |
| **Breaking Changes** | Detects incompatible proto modifications |
| **Formatting** | Consistent indentation, spacing, line endings |

View all rules: https://buf.build/docs/lint/rules

## 🧪 Running the Application

### Run Tests

```bash
./gradlew test
```

### Build JAR

```bash
./gradlew build
ls -lh build/libs/*.jar
```

### Run Application (if main class is configured)

```bash
./gradlew run
```

## 📚 Best Practices Demonstrated

### 1. Proto Design
- ✅ Comprehensive field documentation
- ✅ Clear request/response patterns
- ✅ Resource naming conventions (RFC 6570 URI templates)
- ✅ Output-only fields for server-generated values
- ✅ Required field constraints

### 2. Metadata & Documentation
- ✅ Package-level comments
- ✅ Message-level documentation
- ✅ Field-level descriptions
- ✅ RPC method descriptions
- ✅ Custom metadata for cross-cutting concerns

### 3. Build Automation
- ✅ Linting before compilation (fail-fast)
- ✅ Automatic code generation
- ✅ Format validation and fixing
- ✅ Breaking change detection
- ✅ Dependency management

### 4. Dependencies
- ✅ Official Google API protos via Maven
- ✅ Latest protobuf runtime
- ✅ Proper version management

## 🔗 Hybrid Buf + Gradle Approach

### Why This Architecture?

| Tool | Purpose | Why Use |
|------|---------|---------|
| **Buf** | Proto validation & linting | Enforce standards, detect breaking changes |
| **Gradle** | Build orchestration | Java build, dependency management, code generation |
| **protobuf-gradle-plugin** | Proto to Java | Official plugin for reliable code generation |

### Advantages

- ✅ **Best of Both**: Buf for validation, Gradle for Java integration
- ✅ **No Language Dependencies**: Pure Java/Gradle setup
- ✅ **Official Support**: Uses official protobuf-gradle-plugin
- ✅ **Maven Central**: No external registries needed
- ✅ **Scalable**: Works for large codebases with multiple modules

## 🚀 Next Steps & Extensions

### For Production Use

1. **Add gRPC Support**
   ```gradle
   plugins {
     id 'com.google.protobuf' version '0.9.5'
   }

   protobuf {
     plugins {
       grpc { artifact = 'io.grpc:protoc-gen-grpc-java:1.76.0' }
     }
   }
   ```

2. **Add Service Implementation**
   - Implement PaymentsService interface
   - Add business logic
   - Add database layer

3. **Docker Integration**
   - Create Dockerfile
   - Build container with generated code

4. **API Documentation**
   - Generate OpenAPI/Swagger from protos
   - Use buf's `generate` command with plugins

5. **Testing**
   - Add integration tests
   - Test proto serialization/deserialization

### For Team Collaboration

1. **Pre-commit Hooks**
   ```bash
   ./gradlew bufLint bufFormatCheck
   ```

2. **CI/CD Pipeline**
   - Run linting on PRs
   - Enforce breaking change checks
   - Generate code on main branch

3. **Documentation Generation**
   - Generate proto documentation
   - Publish API reference

## 🔧 Troubleshooting

### Build Fails with "file does not exist"
```bash
# Ensure buf.yaml exists and proto files are in correct location
ls -la src/main/proto/
cat buf.yaml
```

### Linting Errors
```bash
# View detailed lint violations
./gradlew bufLint --info

# Auto-fix formatting issues
./gradlew bufFormatApply
```

### Generated Code Not Found
```bash
# Regenerate proto code
./gradlew clean generateProto

# Check generated location
ls -R build/generated/sources/proto/main/java/
```

## 📖 References

- **Buf Documentation**: https://buf.build/docs
- **Protocol Buffers**: https://protobuf.dev
- **protobuf-gradle-plugin**: https://github.com/google/protobuf-gradle-plugin
- **Google API Annotations**: https://github.com/googleapis/api-common-protos
- **gRPC**: https://grpc.io

## 📄 License

This is a demonstration project. Use as a template for your own projects.

## ✨ Key Accomplishments

- ✅ **Hybrid Buf + Gradle Setup**: Combines Buf validation with Gradle build orchestration
- ✅ **Proto File Split Pattern**: Separated types, messages, and services into logical concerns
- ✅ **gRPC Service Definitions**: Clean RPC method signatures without REST/HTTP bindings
- ✅ **Automatic Documentation**: protoc-gen-doc generates comprehensive HTML API reference
- ✅ **Comprehensive Validation**: bufValidate task combines linting + format checking
- ✅ **DEFAULT Buf Linting**: Enforces naming conventions, documentation requirements, import checks
- ✅ **Java 21 Native Generation**: Automatic proto-to-Java code generation via protobuf-gradle-plugin
- ✅ **Production-Ready Structure**: Follows company-wide best practices and standards

---

**Last Updated**: October 2025
**Proto Version**: v1
**Java Target**: 21 (LTS)
