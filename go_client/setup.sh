#!/bin/bash

# Setup script for Go Client
# Generates Go proto bindings and installs dependencies

set -e

echo "🚀 Setting up Go Payment Transaction Client"
echo "==========================================="

# Check if protoc is installed
if ! command -v protoc &> /dev/null; then
    echo "❌ protoc not found. Please install Protocol Buffers:"
    echo "   macOS: brew install protobuf"
    echo "   Linux: apt-get install protobuf-compiler"
    echo "   Or download: https://github.com/protocolbuffers/protobuf/releases"
    exit 1
fi

echo "✅ protoc found: $(protoc --version)"

# Install Go proto compiler plugins
echo ""
echo "Installing Go proto compiler plugins..."
go install github.com/golang/protobuf/protoc-gen-go@latest
go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@latest
echo "✅ Proto compiler plugins installed"

# Create gen directory
echo ""
echo "Creating gen directory for proto bindings..."
mkdir -p gen

# Generate Go code from proto files
echo ""
echo "Generating Go proto bindings..."
protoc \
  -I../src/main/proto \
  --go_out=gen \
  ../src/main/proto/payments/v1/payment_common.proto \
  ../src/main/proto/payments/v1/transaction.proto

echo "✅ Proto bindings generated in gen/"

# Download Go dependencies
echo ""
echo "Downloading Go dependencies..."
go mod download
echo "✅ Dependencies downloaded"

echo ""
echo "✅ Setup complete!"
echo ""
echo "Next steps:"
echo "  1. Start Java server: cd .. && ./gradlew run"
echo "  2. Run client: go run main.go"
echo "  3. View help: go run main.go help"
