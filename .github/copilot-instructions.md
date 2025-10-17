# Claude Code: Development Guidelines

This document provides guidance for AI coding agents working with this codebase.

## 🎯 Project Overview

This is a **Production-Ready Payment Transaction API** built with:
- **Java 21** for the runtime
- **Gradle 9.0+** for build orchestration
- **Protocol Buffers (protobuf)** for data schemas and service contracts
- **Buf** for proto linting, formatting, and validation
- **protobuf-gradle-plugin** for Java code generation
- **Google API Annotations** for REST HTTP bindings

The project demonstrates a **hybrid Buf + Gradle** approach combining proto validation with reliable Java code generation.

### Directory Structure

```
src/
├── main/
│   ├── java/buf/gradle/demo/          # Java implementation code
│   ├── proto/
│   │   ├── payments/v1/transaction.proto       # Main API definition
│   │   └── schema/meta/v1/meta.proto           # Metadata extensions
│   └── resources/                     # Resource files
└── test/                              # Test files

build/
├── generated/sources/proto/main/java/ # Generated Java code (primary output)
├── extracted-include-protos/          # Extracted proto dependencies from Maven
└── ...
```

## 🔧 Key Technologies

| Component | Tool | Version | Purpose |
|-----------|------|---------|---------|
| **Runtime** | Java | 21 (LTS) | Target platform |
| **Build System** | Gradle | 9.0+ | Build orchestration |
| **Proto Validation** | Buf | 1.53.0 | Linting, formatting, breaking changes |
| **Proto→Java** | protobuf-gradle-plugin | 0.9.5 | Official code generation |
| **Protoc Compiler** | protoc | 4.32.1 | Proto compilation |
| **Google APIs** | proto-google-common-protos | 2.61.2 | Google API protos (Maven dependency) |

## ⚙️ Build Architecture

### Hybrid Approach: Buf + Gradle + protobuf-gradle-plugin

```
Proto Files (src/main/proto)
    ↓
[bufFormatCheck] ← Buf format validation
    ↓
[bufLint] ← Buf DEFAULT linting rules
    ↓
[generateProto] ← protobuf-gradle-plugin generates Java
    ↓
[compileJava] ← Gradle compiles generated + source Java
    ↓
[build] ← Creates JAR artifact
```

**Key Design Decisions:**
- ✅ Use **Buf for validation** (linting, formatting, breaking changes)
- ✅ Use **protobuf-gradle-plugin for Java code generation** (official, reliable)
- ✅ Use **Maven dependencies** for proto imports (no external registries)
- ✅ **Disable Buf's generate task** (we use Gradle's generateProto instead)

## 📋 Build Commands

### Common Tasks

```bash
# Full clean build (recommended)
./gradlew clean build

# Run only linting
./gradlew bufLint

# Apply formatting fixes
./gradlew bufFormatApply

# Generate proto code (manually)
./gradlew generateProto

# Run tests
./gradlew test

# Check for breaking changes
./gradlew bufBreaking

# View Gradle tasks
./gradlew tasks
```

### Development Workflow

1. **Modify or add `.proto` files** in `src/main/proto/`
2. **Format files** (auto-formatting):
   ```bash
   ./gradlew bufFormatApply
   ```
3. **Verify linting** (before building):
   ```bash
   ./gradlew bufLint
   ```
4. **Build and test**:
   ```bash
   ./gradlew build
   ```
5. **Check for breaking changes** (pre-commit):
   ```bash
   ./gradlew bufBreaking
   ```

## 📝 Proto File Guidelines

### Location & Naming
- Place proto files in `src/main/proto/` following domain/version structure
- Example: `src/main/proto/payments/v1/transaction.proto`
- Use semantic versioning in package names: `v1`, `v2`, etc.

### Documentation Requirements
- **All messages** must have comments explaining purpose
- **All fields** must have comments describing the value
- **All services and RPCs** must be documented
- **All enums** should have documentation

Example:
```proto
// Address represents a physical address for billing or shipping.
message Address {
  // Line 1 of the address (street address)
  string line1 = 1 [(google.api.field_behavior) = REQUIRED];

  // City name
  string city = 2 [(google.api.field_behavior) = REQUIRED];
}
```

### Linting Standards
- Project uses **Buf DEFAULT linting rules**
- Run `./gradlew bufLint` to verify compliance
- Run `./gradlew bufFormatApply` to auto-fix style issues
- Do NOT disable linting rules without strong justification

### Breaking Changes
- Proto messages are backward compatible by default (append-only fields)
- Never reuse field numbers
- Use custom options for optional fields, don't remove required fields
- Run `./gradlew bufBreaking` before committing changes to existing APIs

## 🤖 Code Generation

### Generated Java Code

**Input:** Proto files in `src/main/proto/`
**Output:** Generated Java in `build/generated/sources/proto/main/java/`

```
payments/v1/transaction.proto
    ↓
Generates:
├── Transaction.java & TransactionOrBuilder.java
├── Address.java & AddressOrBuilder.java
├── CardHolder.java & CardHolderOrBuilder.java
├── PaymentsServiceGrpc.java (if gRPC plugin enabled)
└── PaymentsProto.java (descriptor holder)
```

### Important Rules for Generated Code

- ✅ Generated code is **automatically included in classpath**
- ✅ Generated code directory is in `.gitignore` (do NOT commit)
- ❌ **Never manually edit** generated Java files
- ❌ **Never remove** proto files without migration plan
- ✅ Always update protos to add/modify functionality

### Making Proto Changes

When you need to change a message structure:

1. **Edit the `.proto` file** in `src/main/proto/`
2. **Document changes** with clear comments
3. **Rebuild** to regenerate Java code:
   ```bash
   ./gradlew clean generateProto
   ```
4. **Use updated Java classes** in your implementation
5. **Run tests** to verify compatibility

## 📚 Proto File Organization

### Current Proto Modules

#### 1. Payment Transaction API (`payments/v1/transaction.proto`)
- **Purpose**: CRUD operations for payment transactions
- **Features**:
  - REST HTTP bindings via `google.api.http`
  - Field constraints via `google.api.field_behavior`
  - Resource patterns via `google.api.resource`
  - Timestamps for audit trails
  - Extensible via custom metadata

#### 2. Schema Metadata (`schema/meta/v1/meta.proto`)
- **Purpose**: Custom extensions for cross-cutting concerns
- **Features**:
  - File-level metadata (domain, owner, tags)
  - Service-level metadata
  - Method-level metadata
  - Message-level metadata
  - Field-level metadata
- **Use Cases**: Audit logging, PII classification, ownership tracking

### Adding New Proto Files

When adding new APIs:

```proto
syntax = "proto3";

// Package for domain APIs (with version)
package mydomain.v1;

import "google/api/annotations.proto";
import "google/api/field_behavior.proto";
import "google/protobuf/timestamp.proto";
import "schema/meta/v1/meta.proto";

option java_multiple_files = true;
option java_package = "com.example.mydomain.v1";

// File-level documentation
option (schema.meta.v1.file_meta) = {
  tags: { key: "x-domain" value: "mydomain" }
  tags: { key: "x-owner" value: "my-team" }
};

// Your messages and services...
```

## 🔒 Configuration Files

### `build.gradle` - Main Build Configuration
**DO:**
- ✅ Add Maven dependencies for proto imports
- ✅ Update protobuf plugin versions for new features
- ✅ Configure Java language version

**DON'T:**
- ❌ Remove `bufLint` dependency from `generateProto`
- ❌ Modify proto source directories without understanding impact
- ❌ Disable format checking

### `buf.yaml` - Buf Configuration
**DO:**
- ✅ Use DEFAULT linting rules
- ✅ Add new proto module paths for multi-module projects
- ✅ Update buf.build dependencies when adding new APIs

**DON'T:**
- ❌ Disable linting rules without documentation
- ❌ Remove breaking change detection
- ❌ Use old Buf v1 configuration

## ✅ Code Review Checklist

When reviewing proto changes, verify:

- [ ] All messages have documentation comments
- [ ] All fields have description comments
- [ ] All RPC methods are documented
- [ ] `bufLint` passes without errors: `./gradlew bufLint`
- [ ] `bufFormatApply` produces no changes: `./gradlew bufFormatApply`
- [ ] `bufBreaking` shows no breaking changes: `./gradlew bufBreaking`
- [ ] Field numbers are never reused
- [ ] Build succeeds: `./gradlew clean build`
- [ ] Tests pass: `./gradlew test`

## 🚀 Common Tasks

### Add a New Message Type

1. Edit appropriate `.proto` file
2. Add message with documentation:
   ```proto
   // MyMessage represents...
   message MyMessage {
     // field_name describes...
     string field_name = 1 [(google.api.field_behavior) = REQUIRED];
   }
   ```
3. Format: `./gradlew bufFormatApply`
4. Build: `./gradlew build`

### Add a New RPC Endpoint

1. Edit service definition in proto file:
   ```proto
   service MyService {
     // MyMethod does...
     rpc MyMethod(MyRequest) returns (MyResponse) {
       option (google.api.http) = {
         post: "/v1/my-endpoint"
         body: "*"
       };
     }
   }
   ```
2. Add corresponding request/response messages
3. Format and lint: `./gradlew bufFormatApply bufLint`
4. Rebuild: `./gradlew clean build`

### Fix Linting Errors

```bash
# View all linting violations
./gradlew bufLint

# Auto-fix formatting issues
./gradlew bufFormatApply

# Re-run build
./gradlew build
```

## 📖 API Documentation Generation

The project includes automated documentation generation for Protocol Buffer APIs using **protoc-gen-doc** (Docker image).

### Documentation Workflow

```
Proto Files (src/main/proto/)
    ↓
[generateProtoDocs] ← Gradle task (requires Docker)
    ↓
HTML Documentation (build/docs/index.html)
    ↓
GitHub Actions → GitHub Pages
```

**Key features:**
- ✅ No Python dependencies (uses protoc-gen-doc Docker image)
- ✅ Automatic HTML generation directly from proto comments
- ✅ Automatically deployed to GitHub Pages on push
- ✅ Minimal build configuration in build.gradle

### Build Commands for Documentation

```bash
# Generate HTML documentation from proto files (requires Docker)
./gradlew generateProtoDocs

# View generated HTML
open build/docs/index.html

# Or serve locally with a simple HTTP server
cd build/docs && python -m http.server 8000
# View at: http://localhost:8000
```

### Documentation Output

After running `./gradlew generateProtoDocs`:

```
build/docs/
├── index.html          # Main documentation page
├── *.js                # JavaScript for search and navigation
└── *.css               # Styling
```

### Automated Deployment

Documentation is **automatically generated and deployed** to GitHub Pages via `.github/workflows/docs.yml`:

1. **Trigger**: Push to main or fix/add-google-api-support branches with changes to:
   - `src/main/proto/**`
   - `build.gradle`
   - `.github/workflows/docs.yml`

2. **Process**:
   - Proto files → HTML (protoc-gen-doc Docker image, triggered in CI)
   - HTML uploaded to GitHub Pages
   - **No local build required** - happens automatically

3. **Live URL**: https://karthik78180.github.io/buf-gradle-demo/

### Documentation Guidelines

When adding or modifying proto files:

1. **Add comprehensive comments** to all messages, fields, and RPCs
2. **Comments become part of auto-generated docs** (protoc-gen-doc parses them)
3. **Commit changes** - docs auto-generate and deploy on push to main
4. Example format:
   ```proto
   // MyService provides operations for managing resources.
   service MyService {
     // GetResource retrieves a resource by its ID.
     rpc GetResource(GetResourceRequest) returns (GetResourceResponse);
   }
   ```

### Tools Used

| Tool | Purpose | Link |
|------|---------|------|
| **protoc-gen-doc** | Proto → HTML | https://github.com/pseudomuto/protoc-gen-doc |
| **Docker** | Container for protoc-gen-doc | https://www.docker.com/ |
| **GitHub Pages** | Free documentation hosting | https://pages.github.com/ |

## 🔗 External References

- **Buf Documentation**: https://buf.build/docs
- **Protocol Buffers**: https://protobuf.dev
- **protobuf-gradle-plugin**: https://github.com/google/protobuf-gradle-plugin
- **Google API Annotations**: https://github.com/googleapis/api-common-protos
- **Gradle**: https://gradle.org/docs
- **protoc-gen-doc**: https://github.com/pseudomuto/protoc-gen-doc

## 📖 Project Documentation

- **README.md**: Project overview, architecture, quick start
- **.github/copilot-instructions.md**: This file - Claude Code guidelines

---

**Last Updated:** October 2025
**Framework Version:** Buf 1.53.0 + protobuf-gradle-plugin 0.9.5
**Java Target:** 21 (LTS)
**Gradle:** 9.0+
