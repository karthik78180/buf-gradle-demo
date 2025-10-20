# Proto Documentation Generation with Buf

## Overview

This project generates Markdown API documentation using **Buf CLI** and `buf.gen.yaml` configuration.

## Setup

### Prerequisites
- Java 21+
- Gradle 9.0.0+ (included via wrapper)

## Generate Documentation

```bash
# Generate proto documentation
./gradlew generateProto

# View generated documentation
cat build/generated/sources/proto/main/doc/index.md
```

**Generated Output:**
- `build/generated/sources/proto/main/doc/index.md` - Complete API reference in Markdown

## Dependency Locking with buf.lock

### What is buf.lock?

`buf.lock` records exact versions of all proto dependencies from `buf.yaml`. It ensures every build uses the same dependency versions across your team and CI/CD.

### How buf.lock is Generated

**Automatically generated during Gradle build:**
```bash
# buf.lock is created/updated automatically when running:
./gradlew build
./gradlew bufLint
./gradlew bufValidate
./gradlew generateProto
```

The `build.buf` Gradle plugin resolves dependencies in `buf.yaml` and writes `buf.lock` automatically.

### When to Regenerate buf.lock

**Update dependencies in buf.yaml, then rebuild:**
```bash
# Edit buf.yaml to add/update dependencies
vim buf.yaml

# Rebuild to update buf.lock
./gradlew clean build
```

### Best Practice

✅ **Commit buf.lock to version control**
- Ensures reproducible builds
- Prevents unwanted dependency updates
- All team members use identical versions

```bash
git add buf.lock
git commit -m "Update proto dependency lock file"
```

**Never edit buf.lock manually** - it's auto-generated and managed by Gradle.

## How It Works

**Flow:**
```
buf.gen.yaml (configuration)
    ↓
./gradlew generateProto
    ↓
bufGenerateDoc task (runs: buf generate)
    ↓
buf reads buf.gen.yaml, finds "doc" plugin
    ↓
calls protoc-gen-doc (must be installed)
    ↓
generates: build/generated/sources/proto/main/doc/index.md
```

**Key Point:** Buf itself doesn't generate documentation. Buf is a wrapper that orchestrates `buf generate` to call the doc plugin (protoc-gen-doc).

## Configuration

**buf.gen.yaml:**
```yaml
version: v1
plugins:
  - name: doc
    out: build/generated/sources/proto/main/doc
    opt:
      - markdown
      - index.md
```

**build.gradle:**
```gradle
// Generate documentation using Buf CLI
task bufGenerateDoc(type: Exec) {
    description = 'Generate proto documentation using Buf CLI'
    group = 'buf'
    commandLine('buf', 'generate')
}

tasks.named('generateProto') {
    dependsOn tasks.named('bufValidate')
    dependsOn tasks.named('bufGenerateDoc')
}
```

## Proto Validation

Buf also validates protos during build:

```bash
# Lint protos
./gradlew bufLint

# Check formatting
./gradlew bufFormatCheck

# Auto-fix formatting
./gradlew bufFormatApply

# Full validation
./gradlew bufValidate
```

## Full Build Process

```bash
# Build with validation and doc generation
./gradlew build
```

Execution order:
1. ✅ `bufValidate` - Lint and format check
2. ✅ `bufGenerateDoc` - Generate documentation via `buf generate`
3. ✅ `generateProto` - Generate Java code
4. ✅ `compileJava` - Compile Java
5. ✅ `test` - Run tests
6. ✅ `build` - Create artifacts

## Quick Commands

```bash
# Lint protos
./gradlew bufLint

# Check formatting
./gradlew bufFormatCheck

# Auto-fix formatting
./gradlew bufFormatApply

# Generate docs only
./gradlew bufGenerateDoc

# Full validation + docs
./gradlew generateProto

# Full build
./gradlew build

# View docs
cat build/generated/sources/proto/main/doc/index.md
```

## Generated Documentation Contents

The documentation includes:
- Table of contents with file organization
- Service definitions with RPC methods
- Message structures with field descriptions
- Field types and labels
- Scalar value types reference
- Cross-linked type references

## Troubleshooting

**Error: "file does not exist" for google imports**
This is normal - buf handles google proto dependencies internally during build.

## See Also

- Buf Documentation: https://buf.build/docs
- protoc-gen-doc: https://github.com/pseudomuto/protoc-gen-doc
- Protocol Buffers: https://protobuf.dev
- Generated Java code: `build/generated/sources/proto/main/java/`
