# Buf Gradle Demo: Payment Transaction API

A demonstration of a production-ready Protocol Buffer (Proto) setup integrating **Buf**, **Gradle**, and **Java 21** for building scalable microservices APIs. This project showcases best practices for proto-driven API development with automated linting, formatting, and code generation.

## 🎯 Project Overview

This project implements a **Payment Transaction Processing API** using Protocol Buffers with:
- ✅ **Buf** for proto validation, linting, and breaking change detection
- ✅ **Gradle** with `protobuf-gradle-plugin` for Java code generation
- ✅ **Google API Annotations** for REST HTTP bindings
- ✅ **Custom Metadata Extensions** for cross-cutting concerns
- ✅ **Comprehensive Documentation** with inline comments and examples

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
| **Proto Validation** | Buf | 1.53.0 |
| **Proto Compilation** | protobuf-gradle-plugin | 0.9.5 |
| **Protoc Compiler** | Protocol Buffers | 4.32.1 |
| **Google APIs** | proto-google-common-protos | 2.61.2 |
| **Testing** | JUnit 5 | 5.10.3 |

## 📁 Project Structure

```
buf-gradle-demo/
├── README.md                              # This file
├── .github/
│   └── copilot-instructions.md           # AI assistant guidelines
├── build.gradle                           # Gradle build configuration
├── buf.yaml                               # Buf linting & module config
├── gradle.properties                      # Gradle properties
├── settings.gradle                        # Gradle multi-project settings
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── buf/gradle/demo/
│   │   │       └── Library.java          # Utility library class
│   │   ├── proto/
│   │   │   ├── payments/v1/
│   │   │   │   └── transaction.proto     # Payment transaction API
│   │   │   └── schema/meta/v1/
│   │   │       └── meta.proto            # Custom metadata extensions
│   │   └── resources/                    # (empty, for resources)
│   └── test/java/                        # Test files
└── build/
    ├── generated/sources/proto/main/java/ # Generated Java code
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
1. ✅ `bufFormatCheck` - Validates proto formatting
2. ✅ `bufLint` - Applies Buf linting rules (DEFAULT)
3. ✅ `generateProto` - Generates Java code from protos
4. ✅ `compileJava` - Compiles Java source code
5. ✅ `test` - Runs tests
6. ✅ `assemble` - Creates JAR artifact

## 📝 Core Proto Definitions

### Payment Transaction API (`payments/v1/transaction.proto`)

Implements a complete CRUD API for payment transactions:

```proto
service PaymentsService {
  rpc CreateTransaction(CreateTransactionRequest) returns (CreateTransactionResponse) {
    option (google.api.http) = {
      post: "/v1/transactions"
      body: "transaction"
    };
  }

  rpc GetTransaction(GetTransactionRequest) returns (GetTransactionResponse) {
    option (google.api.http) = {get: "/v1/{name=transactions/*}"};
  }

  rpc UpdateTransaction(UpdateTransactionRequest) returns (UpdateTransactionResponse) {
    option (google.api.http) = {patch: "/v1/{name=transactions/*}" body: "*"};
  }

  rpc DeleteTransaction(DeleteTransactionRequest) returns (DeleteTransactionResponse) {
    option (google.api.http) = {delete: "/v1/{name=transactions/*}"};
  }
}
```

### Key Features

- **Google API Annotations** for REST HTTP bindings
- **Field Behavior** constraints (REQUIRED, OUTPUT_ONLY)
- **Resource Patterns** for RESTful resource naming
- **Timestamps** for audit trails (create_time, update_time)
- **Metadata Extensions** for domain/owner/PII tracking

### Custom Metadata (`schema/meta/v1/meta.proto`)

Extensible metadata system:

```proto
message Metadata {
  map<string, string> tags = 1;      // Arbitrary key-value metadata
  string owner = 2;                   // Responsible team
  string domain = 3;                  // Business domain
  string pii_level = 4;               // PII classification
}

// Can be attached to:
extend google.protobuf.FileOptions { Metadata file_meta = 50001; }
extend google.protobuf.ServiceOptions { Metadata service_meta = 50002; }
extend google.protobuf.MethodOptions { Metadata method_meta = 50003; }
extend google.protobuf.MessageOptions { Metadata message_meta = 50004; }
extend google.protobuf.FieldOptions { Metadata field_meta = 50005; }
```

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

- ✅ Hybrid Buf + Gradle + protobuf-gradle-plugin setup
- ✅ Payment transaction CRUD API with HTTP bindings
- ✅ Custom metadata extension system
- ✅ Comprehensive proto documentation
- ✅ DEFAULT Buf linting enforcement
- ✅ Automatic code generation to Java 21
- ✅ Production-ready project structure

---

**Last Updated**: October 2025
**Proto Version**: v1
**Java Target**: 21 (LTS)
