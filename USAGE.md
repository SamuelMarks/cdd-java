# Usage Guide for `cdd-java`

`cdd-java` provides a powerful Command-Line Interface (CLI) for bidirectional code and documentation generation.

## Basic Execution

Run the CLI executable from the root of the repository:
```bash
./cdd-java [command] [options]
```

### Global Options
*   `--help`: Prints help documentation.
*   `--version`: Prints the current version.

---

## 1. Generating Java from OpenAPI (`from_openapi`)

Generates dependency-free Java code (Data Models, API Clients, Mock Servers, and integration tests) directly from your `openapi.json` file.

**Usage:**
```bash
./cdd-java from_openapi -i <spec.json>
```

**Example:**
```bash
./cdd-java from_openapi -i my-api.json > GeneratedApi.java
```
*The resulting `GeneratedApi.java` file will contain all necessary `public class` DTOs, a functioning `java.net.http.HttpClient` configured for all described routes, and a `HttpServer` mock implementation.*

---

## 2. Generating OpenAPI from Java (`to_openapi`)

Performs reverse extraction from standard `.java` files to synthesize a compliant `openapi.json` file. It maps `public class` configurations to schemas and `HttpClient` routings to API paths.

**Usage:**
```bash
./cdd-java to_openapi -f <path/to/code>
```

**Example:**
```bash
./cdd-java to_openapi -f src/main/java/com/example/api/ > openapi.json
```
*This command scans the provided directory, interprets the AST using `javaparser-core`, and outputs a valid `openapi.json` representing your current code state.*

---

## 3. Extracting Doc-Friendly JSON (`to_docs_json`)

Transforms complex, deeply nested OpenAPI schemas into a flat, doc-friendly JSON projection (`models`, `routes`) specifically formatting output to be easily consumed by static site generators.

**Usage:**
```bash
./cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json>
```

**Options:**
*   `--no-imports`: Excludes standard model imports from the output.
*   `--no-wrapping`: Unwraps array and object containers for a flatter hierarchy.

**Example:**
```bash
./cdd-java to_docs_json --no-wrapping -i openapi.json > docs.json
```
