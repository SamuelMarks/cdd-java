# Architecture of `cdd-java`

`cdd-java` is designed as a standalone, bidirectional transpiler acting as a bridge between OpenAPI 3.2.0 representations and pure Java code. It operates independently of massive frameworks (like Spring or Netty) to keep the footprint small and the generated artifacts purely standard Java.

## High-Level Operations

The system is built around three core operational modes:

1. **`from_openapi` (Emit):** Reads an `openapi.json` file and synthesizes standard Java code, including Data Models (DTOs), Route Clients (`java.net.http.HttpClient`), Mock Servers (`com.sun.net.httpserver.HttpServer`), and integration tests.
2. **`to_openapi` (Parse):** Analyzes existing `.java` source files using `javaparser-core`, mapping `public class` properties to OpenAPI schemas and `HttpClient` routes to API paths, culminating in a compliant `openapi.json`.
3. **`to_docs_json`:** Flattens complex OpenAPI structures into simplified JSON layouts specifically purposed for static documentation generation.

## Directory Structure & Modules

The internal architecture is split into functional domains located under `src/main/java/`:

* **`cli/`:** Contains the main entrypoint (`Main.java`) which handles argument parsing, delegates execution to the appropriate modules, and outputs results.
* **`openapi/`:** Contains the internal Java representations of the OpenAPI 3.2.0 specification (e.g., `OpenAPI.java`, `Schema.java`, `PathItem.java`). It acts as the canonical data model used by all transformations.
* **`classes/`:** Handles the synthesis (`Emit.java`) and extraction (`Parse.java`) of standard Java classes/DTOs.
* **`routes/`:** Manages the synthesis of `java.net.http.HttpClient` API clients and the reverse parsing of routing logic.
* **`mocks/`:** Responsible for generating zero-dependency `com.sun.net.httpserver.HttpServer` mock instances based on OpenAPI paths.
* **`tests/`:** Generates self-contained execution loops (using Java Reflection) to form a complete testing suite against the generated clients and models.
* **`docstrings/`:** Manages the extraction and injection of JavaDoc comments to ensure the generated code is 100% documented and that OpenAPI descriptions are derived from code comments.
* **`functions/`:** Shared utilities and helper methods spanning the translation lifecycles.

## Core Technologies

* **Language:** Java (JDK 11+ required for Native HTTP Client and Server APIs).
* **Dependencies:**
  * `javaparser-core` for Abstract Syntax Tree (AST) evaluation of `.java` files during reverse extraction (`to_openapi`).
  * `jackson-core`, `jackson-databind`, `jackson-annotations` for JSON serialization and deserialization.
