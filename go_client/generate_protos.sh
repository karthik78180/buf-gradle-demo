#!/bin/bash
set -e

cd "$(dirname "$0")"

echo "Generating Go proto bindings..."

rm -rf gen
mkdir -p gen

/opt/homebrew/bin/protoc \
  -I../src/main/proto \
  --plugin=/Users/kxk78180/go/bin/protoc-gen-go \
  --go_out=gen \
  ../src/main/proto/payments/v1/transaction.proto \
  ../src/main/proto/payments/v1/payment_common.proto

echo "✅ Proto bindings generated!"
find gen -type f
