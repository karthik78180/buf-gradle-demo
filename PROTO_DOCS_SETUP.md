# Proto Documentation Generation

Quick guide to generating API documentation from proto files using Gradle.

## Generate Documentation

```bash
./gradlew generateProto
```

**Output:**
- `build/generated/sources/proto/main/doc/index.md` - Complete API reference in Markdown

View the documentation:
```bash
cat build/generated/sources/proto/main/doc/index.md
```

## What's Included

The generated documentation contains:
- Service definitions and RPC methods
- Message structures and field descriptions
- Type references and constraints
- Complete API reference

## Dependency Locking with buf.lock

### What is buf.lock?

`buf.lock` is a **lock file** that records exact commit hashes and digests of all proto dependencies from `buf.yaml`. It ensures reproducible builds across your team.

**Example content:**
```
deps:
  - name: buf.build/googleapis/googleapis
    commit: 72c8614f3bd0466ea67931ef2c43d608
    digest: b5:13efee...
```

### Commitment to Version Control

✅ **Always commit `buf.lock` to version control**

```bash
git add buf.lock
git commit -m "Commit proto dependency lock file"
```

This ensures all developers build with identical dependency versions.

### When Dependency Changes Are Needed

If you need to add or update proto dependencies in `buf.yaml`, the lock file is managed through the Buf ecosystem and reflects your configuration choices across your team.

## References

- [Buf Documentation](https://buf.build/docs)
- [Protocol Buffers](https://protobuf.dev)
- [Project README](./README.md) - Full build guide and proto design patterns
