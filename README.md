# Buf Gradle Demo: Payment Transaction API

A production-ready example of Protocol Buffers with **Buf**, **Gradle**, and **Java 21**. Demonstrates best practices for proto-driven API development with automated validation, formatting, and code generation.

## 🎯 Project Overview

This project implements a **Payment Transaction Processing API** using Protocol Buffers with:
- ✅ **Buf** for proto validation, linting, and format enforcement
- ✅ **Gradle** with `protobuf-gradle-plugin` for Java code generation
- ✅ **protoc-gen-doc** for automatic API documentation
- ✅ **gRPC service definitions** with clean RPC method signatures
- ✅ **Comprehensive inline documentation** with field constraints

## 🏗️ Architecture

### Hybrid Buf + Gradle Setup

```
┌─────────────────────────────────────────────────────────────┐
│                    Build Pipeline                           │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  bufFormatCheck ──→ bufLint ──→ generateProto ──→ compileJava
│  (Format)         (Lint)      (Proto→Java)      (Java Build)
│                                (protobuf-plugin)             │
│                                                               │
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
| **Documentation** | protoc-gen-doc | 1.5.1 |
| **Google APIs** | proto-google-common-protos | 2.61.2 |
| **Testing** | JUnit 5 | 5.10.3 |

## 📁 Project Structure

```
buf-gradle-demo/
├── README.md                              # This file
├── PROTO_DOCS_SETUP.md                    # Documentation generation guide
├── build.gradle                           # Gradle configuration
├── buf.yaml                               # Buf linting & dependencies
├── buf.lock                               # Dependency lock file
├── src/main/proto/
│   ├── payments/v1/
│   │   ├── transaction.proto              # Transaction messages
│   │   └── types.proto                    # Shared types
│   └── schema/service/v1/
│       └── service.proto                  # gRPC service definitions
└── build/generated/
    └── sources/proto/main/
        ├── java/                          # Generated Java code
        └── doc/                           # Generated documentation
```

## 🚀 Quick Start

### Prerequisites
- **Java 21+** installed
- **Gradle 9.0.0+** (included via wrapper)
- **Git** for version control

### Build the Project

```bash
# Clean build with validation and code generation
./gradlew clean build

# Run linting only
./gradlew bufLint

# Auto-fix formatting issues
./gradlew bufFormatApply

# View generated code
ls build/generated/sources/proto/main/java/com/example/payments/v1/
```

### Build Task Execution Flow

```bash
./gradlew build
```

Runs in order:
1. `bufFormatCheck` - Validates proto formatting
2. `bufLint` - Applies linting rules (DEFAULT set)
3. `bufValidate` - Combined format + lint validation
4. `generateProto` - Generates Java code + documentation
5. `compileJava` - Compiles Java source
6. `test` - Runs tests
7. `assemble` - Creates JAR artifact

## 🛡️ Proto Validation with Buf

### Buf Commands

```bash
./gradlew bufLint              # Check proto compliance
./gradlew bufFormatCheck       # Validate formatting
./gradlew bufFormatApply       # Auto-fix formatting
./gradlew bufValidate          # Combined check (format + lint)
```

### Linting Rules (DEFAULT set)

- **Naming**: Package versioning, PascalCase messages, snake_case fields
- **Documentation**: Required comments on messages, fields, RPCs
- **Imports**: Detects unused imports and cyclic dependencies
- **Formatting**: Consistent indentation (2 spaces), spacing, line endings

See all rules: https://buf.build/docs/lint/rules

## 📚 Documentation Generation

### Generate Documentation

```bash
./gradlew generateProto
```

**Output:**
- `build/generated/sources/proto/main/doc/index.html` - Complete API reference

### Generated Contents

The documentation includes:
- Service definitions with RPC methods
- Message structures and field definitions
- Type references and constraints
- Complete API reference with navigation

For detailed guide: See [PROTO_DOCS_SETUP.md](./PROTO_DOCS_SETUP.md)

## ⚙️ Configuration

### buf.yaml - Proto Configuration

```yaml
version: v2
modules:
  - path: src/main/proto
deps:
  - buf.build/googleapis/googleapis  # Google API protos
lint:
  use:
    - DEFAULT                        # Use DEFAULT linting rules
breaking:
  use:
    - FILE                          # Detect file-level breaking changes
```

### buf.lock - Dependency Lock File

Records exact versions of proto dependencies for reproducible builds:

```
deps:
  - name: buf.build/googleapis/googleapis
    commit: 72c8614f3bd0466ea67931ef2c43d608
    digest: b5:13efee...
```

✅ **Always commit buf.lock** to ensure all developers use identical dependency versions.

### build.gradle - Key Configuration

- **Plugins**: Java, Gradle, protobuf-gradle-plugin, build.buf
- **Protoc**: Version 4.32.1 from Maven Central
- **Proto Plugin**: protoc-gen-doc for documentation generation
- **Source**: Proto files in `src/main/proto/`
- **Output**: Generated Java code in `build/generated/sources/proto/main/java/`

## 📊 Generated Code Location

After building, Java classes appear in:

```
build/generated/sources/proto/main/java/com/example/payments/v1/
├── Transaction.java
├── Address.java
├── CardHolder.java
├── Store.java
├── CreateTransactionRequest.java
└── (auto-generated request/response classes)
```

## 🧪 Running the Application

```bash
# Run tests
./gradlew test

# Build JAR
./gradlew build
ls -lh build/libs/*.jar

# Run application (if main class configured)
./gradlew run
```

## 🔍 Proto Design Highlights

The project demonstrates:
- **Separation of Concerns**: Shared types, domain messages, service definitions in separate files
- **Field Constraints**: Using `REQUIRED` and `OUTPUT_ONLY` annotations
- **Timestamps**: Server-generated `create_time` and `update_time`
- **Idempotency**: Safe retries with `client_token`
- **Clean Services**: Pure gRPC RPC signatures without HTTP bindings

## 🔗 Hybrid Buf + Gradle Approach

| Tool | Purpose |
|------|---------|
| **Buf** | Proto validation, linting, breaking change detection |
| **Gradle** | Build orchestration, Java integration, dependency management |
| **protobuf-gradle-plugin** | Reliable proto-to-Java compilation |

**Advantages:**
- Best of both tools: Buf for quality gates, Gradle for Java build
- No external CLI dependencies beyond Gradle
- Official protobuf plugin support
- Scalable for multi-module projects

## 🔧 Troubleshooting

### Build Fails with "file does not exist"
```bash
ls -la src/main/proto/
cat buf.yaml
```

### Linting Errors
```bash
./gradlew bufLint --info
./gradlew bufFormatApply  # Auto-fix
```

### Generated Code Not Found
```bash
./gradlew clean generateProto
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

---

**Last Updated**: October 2025 | **Proto Version**: v1 | **Java Target**: 21 (LTS)
