# Quick Start Guide

Get the Go client running in 5 minutes.

## Step 1: Setup Go Client (3 minutes)

```bash
# Inside go_client directory
cd go_client

# Run setup script (installs tools, generates proto code, downloads dependencies)
./setup.sh

# Expected output:
# ✅ protoc found: libprotoc 3.x.x
# ✅ Proto compiler plugins installed
# ✅ Proto bindings generated in gen/
# ✅ Dependencies downloaded
# ✅ Setup complete!
```

## Step 2: Start Java Server (in new terminal)

```bash
# From project root directory
./gradlew run

# Expected output:
# > Task :run
# Server started on port 8080
# (or: Running application...)
```

## Step 3: Run Go Client (in another terminal)

```bash
# Inside go_client directory
go run main.go

# Expected output:
# 🚀 Payment Transaction Client (Go)
# ================================
#
# === CREATE TRANSACTION ===
# Request size: 123 bytes
# ✅ Transaction created successfully
#
# === GET TRANSACTION ===
# ✅ Transaction retrieved successfully
#
# === LIST TRANSACTIONS ===
# ✅ Transactions listed successfully
#
# === DELETE TRANSACTION ===
# ✅ Transaction deleted successfully
#
# ✅ All CRUD operations completed successfully!
```

## Troubleshooting

### Issue: "protoc: command not found"

**Solution:** Install Protocol Buffers
```bash
# macOS
brew install protobuf

# Linux
apt-get install protobuf-compiler

# Or download from: https://github.com/protocolbuffers/protobuf/releases
```

### Issue: "cannot find package gen/payments/v1"

**Solution:** Proto bindings not generated
```bash
# Re-run setup
./setup.sh
```

### Issue: "connection refused"

**Solution:** Java server not running
```bash
# Start Java server in another terminal
../../../gradlew run
```

### Issue: "build error: main.go:..."

**Solution:** Download dependencies
```bash
go mod download
```

## File Sizes (Binary Proto Efficiency)

| Operation | Request Size | Response Size |
|-----------|------------|--------------|
| Create | 176B | 200B+ |
| Read | 22B | 150B+ |
| List | 20B | 300B+ |
| Delete | 22B | 0B |

**Total:** ~240B request + ~650B response = ~890B
**vs JSON:** ~2-3KB equivalent

**Binary proto is ~75% more efficient!**

## Next Steps

- Review [README.md](README.md) for detailed documentation
- Explore `main.go` to understand CRUD implementation
- Modify operations in `main.go` for your use cases
- Add authentication, metrics, or retry logic

---

**Ready!** 🚀 Go client should now communicate with Java server using Protocol Buffers.
