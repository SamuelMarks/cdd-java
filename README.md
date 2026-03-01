![Doc Coverage](https://img.shields.io/badge/Doc_Coverage-100%25-brightgreen.svg)
![Test Coverage](https://img.shields.io/badge/Test_Coverage-100%25-brightgreen.svg)
# CDD Java (cdd-java)

A bidirectional transpiler that acts as a bridge between OpenAPI 3.2.0 representations and pure Java code.

Designed under strict **Contract-Driven Development** principles.

`cdd-java` provides full 100% interoperability between language and documentation.

## Overview
`cdd-java` allows you to:
1. **Emit (from_openapi):** Generate 100% dependency-free (`java.net.http`, `com.sun.net.httpserver`) Route Clients, Data Models (DTOs), Mock Servers, and integration tests directly from your `openapi.json`.
2. **Parse (to_openapi):** Perform reverse extraction from standard, untampered Java class and routing files to synthesize a compliant `openapi.json` file.
3. **Docs Extraction (to_docs_json):** Translate standard OpenAPI structures into flattened, un-wrapped JSON formats uniquely suited for simplified doc-generation.

## Installation

```bash
# Clone the repository
git clone <your-repo>/cdd-java
cd cdd-java

# Ensure the executable has correct permissions
chmod +x cdd-java
```

*(Note: It requires Jackson JSON mapping libraries under `lib/`. A JDK environment >= 11 is required due to `java.net.http.HttpClient` usage).*

## CLI Usage

The CLI—at a minimum—has:

### `cdd-java --help`
Prints the help documentation.

### `cdd-java --version`
Prints the version (`1.0.0`).

### `cdd-java from_openapi -i <spec.json>`
Generates code artifacts from the OpenAPI specification and prints them. This includes Data Models, API Clients, Mock Server configurations, and testing stubs.

**Example:**
```bash
./cdd-java from_openapi -i api.json > Output.java
```

### `cdd-java to_openapi -f <path/to/code>`
Parses standard `.java` files within the targeted directory or file and constructs a normalized OpenAPI 3.2.0 description mapping the discovered `public class` configurations to schemas and `HttpClient` routings to API paths.

**Example:**
```bash
./cdd-java to_openapi -f src/main/java/ > generated_openapi.json
```

### `cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json>`
Transforms complex nested OpenAPI schemas (`components.schemas`, `paths`) into a flat, doc-friendly JSON projection (`models`, `routes`) specifically formatting output to align with the referenced `@TO_DOCS_JSON.md` specifications.

**Example:**
```bash
./cdd-java to_docs_json --no-wrapping -i api.json
```

## Architecture

* **Models / DTOs:** Uses standard Java `public class` syntax with primitive wrappers (`Integer`, `Double`, `String`, etc.).
* **Routing:** Implements Native `java.net.http.HttpClient`.
* **Mock Servers:** Uses `com.sun.net.httpserver.HttpServer`. Zero external Spring or Netty overhead.
* **Testing:** Emits a `main` execution loop utilizing Java Reflection and assertions to establish a complete testing suite.

## Development Constraints Addressed
- **100% Core Java:** Uses no third party library abstractions besides basic Jackson.
- **100% Documented:** Every generated and internal class contains Javadoc `/** ... */` annotations.
- **100% Tested:** Features a custom reflection-based coverage suite spanning all internal system configurations.