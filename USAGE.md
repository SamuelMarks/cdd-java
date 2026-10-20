# Usage Guide (`cdd-java`)

`cdd-java` can be invoked either as a command-line compiler (via the standalone shaded JAR or `make run`) or programmatically as a Java library.

---

## 1. Running the CLI

Build and invoke `cdd-java` using Maven or `make`:

```sh
# Via Makefile
make run -- --help

# Via standalone shaded JAR
mvn clean package -DskipTests
java -jar target/cdd-java-0.0.4.jar --help
```

### OpenAPI 3.2.0 / Swagger 2.0 Workflows (`from_openapi` ⇄ `to_openapi`)

```sh
# Generate a Java Client SDK (with JUnit 4 tests & mock servers)
cdd-java from_openapi to_sdk -i openapi.json -o ./generated-sdk --tests

# Generate a standalone CLI application wrapping the SDK
cdd-java from_openapi to_sdk_cli -i openapi.json -o ./generated-cli --tests

# Generate a full-stack modular server (Javalin 5, Hibernate 6 ORM, DAOs, DataFaker Seeder, JUnit 4 tests)
cdd-java from_openapi to_server -i openapi.json -o ./generated-server

# Batch-generate from a directory of OpenAPI/Swagger specs
cdd-java from_openapi to_sdk --input-dir ./specs -o ./generated-sdk

# Skip generating .github/workflows/ci.yml or pom.xml scaffolding
cdd-java from_openapi to_sdk -i openapi.json -o ./generated-sdk --no-github-actions --no-installable-package

# Extract an OpenAPI 3.2.0 specification from existing Java source code
cdd-java to_openapi -i ./src/main/java -o openapi.json
```

### Arazzo 1.1.0 Workflows (`from_arazzo` ⇄ `to_arazzo`)

```sh
# Generate a Java workflow SDK orchestrator (with E2E tests and stateful mocks)
cdd-java from_arazzo to_sdk -i arazzo.json -o ./generated-workflows --tests

# Generate a standalone CLI workflow runner (WorkflowCli.java)
cdd-java from_arazzo to_sdk_cli -i arazzo.json -o ./generated-workflow-cli --tests

# Generate server-side E2E workflow integration tests and retry-aware mock simulators
cdd-java from_arazzo to_server -i arazzo.json -o ./generated-workflow-server

# Reverse-parse Java workflow orchestrator, CLI, test, or mock code back to Arazzo 1.1.0
cdd-java to_arazzo -i ./generated-workflows -o arazzo.json
```

### In-Place AST Synchronization (`sync`)

```sh
# Synchronize all packages in-place using lexical-preserving AST updates
cdd-java sync -i ./my-project

# Synchronize into a separate output directory using ORM entities as the source of truth
cdd-java sync -i ./my-project -o ./my-project-synced --truth orm
```

Supported `--truth` values:
- `openapi`: Uses `openapi.json` in the target directory as the authoritative specification.
- `orm`: Extracts schema definitions exclusively from Jakarta Persistence (`@Entity`) classes.
- `classes`: Extracts schema definitions from Jackson model POJOs.
- *(default)*: Aggregates paths, schemas, and workflows across `routes`, `mocks`, `cli`, `tests`, `functions`, `classes`, `orm`, and `workflows`.

### Documentation JSON Generation (`to_docs_json`)

```sh
# Emit documentation JSON containing Java SDK usage snippets per operation
cdd-java to_docs_json -i openapi.json -o docs.json

# Emit bare method call snippets without imports or class/main wrapper boilerplate
cdd-java to_docs_json --no-imports --no-wrapping -i openapi.json -o docs.json
```

### Model Context Protocol (MCP) & JSON-RPC 2.0 Server (`mcp` / `serve_json_rpc`)

```sh
# Start an MCP / JSON-RPC 2.0 HTTP server on 127.0.0.1:8080 (also reading STDIO)
cdd-java mcp --port 8080 --listen 127.0.0.1
cdd-java serve_json_rpc --port 8080 --listen 127.0.0.1

# Start in pure STDIO mode (for MCP hosts and WASI environments)
cdd-java mcp --wasi
```

### In-Memory Compilation (`process_in_memory`)

```sh
cdd-java process_in_memory '{ "command": ["from_openapi", "to_sdk"], "files": { "spec.json": "{ \"openapi\": \"3.2.0\", \"info\": { \"title\": \"Demo\", \"version\": \"1.0.0\" } }" } }'
```

---

## 2. Environment Variables

Every CLI option can also be configured via environment variables:

| Environment Variable | Equivalent Flag | Description |
| :--- | :--- | :--- |
| `CDD_INPUT` | `-i`, `--input` | Input specification file or source directory path. |
| `CDD_INPUT_DIR` | `--input-dir` | Directory containing multiple specification files. |
| `CDD_OUTPUT` | `-o`, `--output` | Output file or target directory path. |
| `CDD_TESTS` | `--tests` | Enable test and mock server generation (`true` or `1`). |
| `CDD_NO_GITHUB_ACTIONS` | `--no-github-actions` | Disable `.github/workflows/ci.yml` scaffolding (`true` or `1`). |
| `CDD_NO_INSTALLABLE_PACKAGE` | `--no-installable-package` | Disable `pom.xml` scaffolding (`true` or `1`). |
| `CDD_NO_IMPORTS` | `--no-imports` | Strip `import` statements in `to_docs_json` (`true` or `1`). |
| `CDD_NO_WRAPPING` | `--no-wrapping` | Strip outer class/`main` wrapper in `to_docs_json` (`true` or `1`). |
| `CDD_PORT` | `-p`, `--port` | TCP port for `serve_json_rpc` / `mcp` (default: `8080`). |
| `CDD_LISTEN` | `-l`, `--listen` | Bind address for `serve_json_rpc` / `mcp` (default: `127.0.0.1`). |
| `CDD_TRUTH` | `--truth` | Source of truth for `sync` (`openapi`, `orm`, `classes`). |
| `CDD_WASI` | `--wasi` | Run `serve_json_rpc` / `mcp` in STDIO mode (`true` or `1`). |
| `CDD_WASI_VIRTUAL_ROOT` | — | Virtual root prefix used to resolve absolute paths in WASI mode. |
