# Payment Transaction API Documentation

Welcome to the Protocol Buffer documentation for the Payment Transaction API. This comprehensive reference covers all messages, services, and APIs for payment transaction processing.

## Quick Start

### Building Documentation Locally

```bash
# 1. Generate markdown from proto files
./gradlew generateProtoDocs

# 2. Install MkDocs and Material theme
pip install mkdocs mkdocs-material

# 3. Serve documentation locally
mkdocs serve

# View at: http://localhost:8000
```

### Deploying to GitHub Pages

Documentation is automatically built and deployed to GitHub Pages on every push to main.

**Live Documentation**: [https://karthik78180.github.io/buf-gradle-demo/](https://karthik78180.github.io/buf-gradle-demo/)

## About This Project

This is a production-ready demonstration of Protocol Buffer API development with:

- **Buf**: Proto validation, linting, and format checking
- **Gradle**: Build orchestration and Java code generation
- **protobuf-gradle-plugin**: Official Java code generation from proto files
- **Google API Annotations**: REST HTTP bindings and field constraints
- **MkDocs**: Beautiful static documentation generation
- **GitHub Pages**: Free hosting for documentation

## Documentation Structure

### API Reference
- **Messages**: Core data structures (Transaction, Address, CardHolder, etc.)
- **Services**: gRPC services and RPC operations
- **Custom Options**: Metadata extensions for cross-cutting concerns

### Key Features

#### Message Documentation
Every message is fully documented with:
- Purpose and usage description
- Field details and constraints
- Field behavior annotations (REQUIRED, OUTPUT_ONLY, etc.)
- Type information and references

#### Service Documentation
Every RPC service includes:
- Service overview and purpose
- Individual RPC method documentation
- Request and response message references
- HTTP binding information (REST endpoints)

#### Custom Metadata
The Payment API includes custom metadata extensions:
- File-level metadata (domain, owner)
- Service-level metadata
- Message-level metadata (entity types, categories)
- Field-level metadata (PII classification)

## Key Concepts

### Transaction Processing
The Payment Transaction API provides CRUD operations for managing payment transactions:

- **Create**: Register a new payment transaction
- **Get**: Retrieve transaction details
- **Update**: Modify transaction information
- **Delete**: Remove a transaction
- **List**: Retrieve multiple transactions with pagination

### Supported Operations

Each transaction includes:
- Cardholder information (name, email, address)
- Store details (ID, name, location)
- Product merchandise items (SKU, quantity, unit price)
- Transaction metadata (creation time, update time)
- Idempotency tokens for request deduplication

## Navigation

Use the left navigation menu to explore:

- **API Reference**: Complete Protocol Buffer documentation
- **Proto Files**: Detailed documentation for each proto file

## Getting Started

1. **Read the API Reference** to understand available messages and services
2. **Check Proto Files** for detailed inline documentation
3. **Review Examples** in the [GitHub repository](https://github.com/karthik78180/buf-gradle-demo)
4. **Generate Java Code** using `./gradlew build`

## Build Instructions

### Prerequisites
- Java 21 or later
- Gradle 9.0 or later (included via wrapper)
- Docker (for documentation generation)

### Build Commands

```bash
# Full build with testing
./gradlew clean build

# Generate documentation
./gradlew generateProtoDocs

# Serve documentation locally
mkdocs serve

# Deploy documentation (automatic via GitHub Actions)
# Just push to main branch
```

## Related Resources

- **Repository**: [github.com/karthik78180/buf-gradle-demo](https://github.com/karthik78180/buf-gradle-demo)
- **Buf Documentation**: [buf.build/docs](https://buf.build/docs)
- **Protocol Buffers**: [protobuf.dev](https://protobuf.dev)
- **MkDocs**: [mkdocs.org](https://www.mkdocs.org/)

## Questions or Issues?

For questions about this documentation or the proto definitions, please:
1. Check the [GitHub repository](https://github.com/karthik78180/buf-gradle-demo)
2. Review the inline documentation in proto files
3. Open an issue on GitHub

---

*Documentation auto-generated from Protocol Buffer definitions using protoc-gen-doc and MkDocs*
