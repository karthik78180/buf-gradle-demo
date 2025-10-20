# Quick Start Guide

Get the Node.js client running in 5 minutes.

## Step 1: Install Node.js (if needed)

```bash
# Check if Node.js is installed
node --version

# If not installed, install from https://nodejs.org (14+ recommended)
```

## Step 2: Install Dependencies (2 minutes)

```bash
# Inside node_client directory
cd node_client
npm install

# Expected output:
# added 1 package, and audited 2 packages in 0.5s
```

## Step 3: Start Java Server (in new terminal)

```bash
# From project root directory
./gradlew run

# Expected output:
# ✅ Payment Transaction Server started on http://localhost:8080
# Press Ctrl+C to stop...
```

## Step 4: Run Node Client (in another terminal)

```bash
# Inside node_client directory
node client.js

# Expected output:
# 🚀 Payment Transaction Client (Node.js)
# ================================
#
# === CREATE TRANSACTION ===
# Request size: 176 bytes
# ✅ CREATE successful!
#    Response size: 187 bytes
#
# === GET TRANSACTION ===
# Request size: 22 bytes
# ✅ GET successful!
#    Response size: 163 bytes
#
# === LIST TRANSACTIONS ===
# Request size: 20 bytes
# ✅ LIST successful!
#    Response size: 306 bytes
#
# === DELETE TRANSACTION ===
# Request size: 22 bytes
# ✅ DELETE successful!
#    Response size: 0 bytes
#
# ✅ All CRUD operations completed successfully!
```

## Troubleshooting

### Issue: "Cannot find module 'protobufjs'"

**Solution:** Run npm install
```bash
npm install
```

### Issue: "connection refused"

**Solution:** Java server not running
```bash
# Start Java server in another terminal
../../../gradlew run
```

### Issue: "ENOENT: no such file or directory"

**Solution:** Proto files not generated
```bash
# Generate proto resources from parent directory
../../../gradlew generateProto
```

## File Sizes (Binary Proto Efficiency)

| Operation | Request Size | Response Size |
|-----------|------------|--------------|
| Create | 176B | 187B |
| Read | 22B | 163B |
| List | 20B | 306B |
| Delete | 22B | 0B |

**Total:** ~896B
**vs JSON:** ~2,500B
**Efficiency:** 65% smaller with binary proto!

## Next Steps

- Review [README.md](README.md) for detailed documentation
- Explore `client.js` to understand CRUD implementation
- Modify operations in `client.js` for your use cases
- Add authentication, metrics, or retry logic

---

**Ready!** 🚀 Node.js client should now communicate with Java server using Protocol Buffers.
