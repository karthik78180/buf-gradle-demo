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
- **Purpose**: Shared metadata extensions for RPC services
- **Features**:
  - `service_meta` extension for RPC services (ServiceOptions)
  - `Metadata` with tags map for service classification (tier, domain, etc.)
- **Use Cases**: Service classification, ownership tracking, tier assignment

### Project Configuration

**Single Source of Truth:** `.github/project-config.yaml`

Update this file with your project's metadata. The values are displayed in generated documentation:

```yaml
projectId: buf-gradle-demo
authenticationType: JWT
documentationUrl: https://karthik78180.github.io/buf-gradle-demo/
projectTitle: Payment Transaction API Service Suite
```

For new projects, copy this file and update the values.

### Adding New Proto Files - Company-Wide Pattern

**IMPORTANT:** Follow the **Service Definition Template** (`.github/SERVICE_DEFINITION_TEMPLATE.md`)

The template shows the standard pattern used across all company repositories:
- **Separate concerns** - Messages in one file, services in another
- **Reusable structure** - Same pattern for all domains
- **Metadata standardization** - Shared metadata extensions

**File structure:**
```
src/main/proto/
├── {domain}/v1/
│   └── {domain}.proto              # Messages, enums, types (versioned per domain)
└── schema/
    ├── service/v1/
    │   └── service.proto           # ALL services (centralized in one file)
    └── meta/v1/
        └── meta.proto              # Shared metadata extensions
```

When adding messages to a domain proto file:

```proto
syntax = "proto3";

// Package for domain APIs (with version)
package mydomain.v1;

import "google/api/field_behavior.proto";
import "google/protobuf/timestamp.proto";

option java_multiple_files = true;
option java_package = "com.example.mydomain.v1";

// Your message definitions only (no services or metadata)
message MyMessage {
  string id = 1 [(google.api.field_behavior) = REQUIRED];
  string name = 2;
}
```

For services, add them to the **centralized** `schema/service/v1/service.proto` file:

```proto
// File-level comments are displayed in generated documentation before the TOC:
//
// # My API Service Suite
//
// **Project Details:**
// - **Project ID:** my-project
// - **Authentication:** JWT
// - **Documentation:** https://example.com/my-project/

service MyService {
  option (schema.meta.v1.service_meta) = {
    tags: { key: "x-domain" value: "mydomain" }
    tags: { key: "x-service-tier" value: "core" }
  };

  rpc CreateMyResource(CreateMyResourceRequest) returns (CreateMyResourceResponse) {
    option (google.api.http) = {
      post: "/v1/myresources"
      body: "resource"
    };
  }
}
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

## 🔄 Git Workflow

### Important Guidelines for AI Agents

**DO NOT automatically push changes.** Always ask for confirmation before performing git operations.

### Git Operations Workflow

1. **Make code changes** - Edit files as requested
2. **Run tests/builds** - Verify changes work locally:
   ```bash
   ./gradlew clean build
   ./gradlew bufLint
   ./gradlew bufFormatApply
   ```
3. **Review changes** - Show `git status` and `git diff` to user
4. **Ask for confirmation** - Display what will be committed
5. **Create commit** - Only after user approves
6. **Ask before push** - Never push to remote without explicit confirmation

### Safe Git Operations

```bash
# Always safe - shows what changed
git status
git diff

# Check before committing
git diff --cached

# Only after user confirmation
git add <files>
git commit -m "message"
git push origin <branch>
```

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

The project includes automated documentation generation for Protocol Buffer APIs using **protoc-gen-doc** integrated via Gradle's protobuf plugin.

### Documentation Workflow

```
Proto Files (src/main/proto/)
    ↓
[generateProto] ← protobuf-gradle-plugin with doc plugin
    ↓
HTML Documentation (build/generated/sources/proto/main/doc/index.html)
    ↓
GitHub Actions → GitHub Pages
```

**Key features:**
- ✅ Pure Gradle implementation (no Docker, Python, or external tools)
- ✅ Automatic HTML generation from proto comments via protoc-gen-doc plugin
- ✅ Services displayed prominently in generated documentation
- ✅ Integrated into standard Gradle build pipeline
- ✅ Automatically deployed to GitHub Pages on push

### Build Commands for Documentation

```bash
# Generate HTML documentation
./gradlew generateProto

# View generated HTML
open build/generated/sources/proto/main/doc/index.html

# Or serve locally
cd build/generated/sources/proto/main/doc && python -m http.server 8000
# View at: http://localhost:8000
```

### Documentation Output

After running `./gradlew generateProto`:

```
build/generated/sources/proto/main/doc/
├── index.html          # Main documentation page (39KB+)
│                       # Contains all services (CreateTransaction, GetTransaction, etc.)
│                       # Followed by message and type definitions
└── (styles embedded in HTML)
```

Documentation follows the pattern: **CRUD*.v1** (e.g., CreateTransaction.v1, GetTransaction.v1)

### Automated Deployment

Documentation is **automatically generated and deployed** to GitHub Pages via `.github/workflows/docs.yml`:

1. **Trigger**: Push to main or fix/add-google-api-support branches with changes to:
   - `src/main/proto/**`
   - `build.gradle`
   - `.github/workflows/docs.yml`

2. **Process**:
   - `./gradlew generateProto` generates HTML from proto files
   - HTML stored in `build/generated/sources/proto/main/doc/`
   - Uploaded to GitHub Pages
   - **No local dependencies required** - Java 21 is all you need

3. **Live URL**: https://karthik78180.github.io/buf-gradle-demo/

### Documentation Guidelines

When adding or modifying proto files:

1. **Add comprehensive comments** to all messages, fields, and RPCs
2. **Comments become part of auto-generated docs** (protoc-gen-doc parses them)
3. **Services are displayed first** in the generated HTML documentation (via custom template)
4. **Run `./gradlew generateProto`** locally to preview documentation
5. **Commit changes** - docs auto-generate and deploy on push to main
6. Example format:
   ```proto
   // PaymentsService provides operations for payment transactions.
   service PaymentsService {
     // CreateTransaction creates a new payment transaction.
     rpc CreateTransaction(CreateTransactionRequest) returns (CreateTransactionResponse);
   }
   ```

### Custom Documentation Template

The project uses a **custom HTML template** (`.github/proto-doc.tmpl`) that customizes the generated documentation:

**What's different:**
- ✅ **Services rendered first** in both Table of Contents and main content
- ✅ Services are marked with `[S]` badge
- ✅ HTTP method patterns displayed (e.g., `/CreateTransaction.v1`)
- ✅ Messages, Enums, Extensions follow services

**Template location:** `.github/proto-doc.tmpl`

**To modify the template:**
1. Edit `.github/proto-doc.tmpl` (Go template syntax)
2. Run `./gradlew generateProto` to regenerate docs
3. Review changes in `build/generated/sources/proto/main/doc/index.html`

**Template syntax reference:** https://github.com/pseudomuto/protoc-gen-doc/wiki/Custom-Templates

### Tools Used

| Tool | Purpose | Link |
|------|---------|------|
| **protoc-gen-doc** | Proto → HTML plugin | https://github.com/pseudomuto/protoc-gen-doc |
| **protobuf-gradle-plugin** | Gradle integration | https://github.com/google/protobuf-gradle-plugin |
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
