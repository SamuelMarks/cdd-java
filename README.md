cdd-java: Java ⇄ OpenAPI
========================================

[![License](https://img.shields.io/badge/license-Apache--2.0%20OR%20MIT-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![interactive WASM web demo](https://img.shields.io/badge/interactive-WASM_web_demo-blueviolet?logo=webassembly)](https://offscale.github.io/cdd-java/)
[![CI](https://github.com/SamuelMarks/cdd-java/actions/workflows/ci.yml/badge.svg)](https://github.com/SamuelMarks/cdd-java/actions/workflows/ci.yml)
[![Test Coverage](https://img.shields.io/badge/coverage-100%25-brightgreen.svg)](https://github.com/offscale/cdd-java/actions)
[![Doc Coverage](https://img.shields.io/badge/docs-100%25-brightgreen.svg)](https://offscale.github.io/cdd-java/)
[![OpenAPI 3.2.0](https://img.shields.io/badge/OpenAPI-3.2.0-6BA539?logo=openapiinitiative&logoColor=white)](https://spec.openapis.org/oas/v3.2.0)
[![Arazzo 1.1.0](https://img.shields.io/badge/Arazzo-1.1.0-6BA539?logo=openapiinitiative&logoColor=white)](https://spec.openapis.org/arazzo/v1.1.0)
[![Model Context Protocol](https://img.shields.io/badge/MCP-1.0.0-black)](https://modelcontextprotocol.io/)
[![Swagger 2.0](https://img.shields.io/badge/Swagger-2.0-85EA2D?logo=swagger&logoColor=black)](https://swagger.io/specification/v2/)

Compiler Driven Development (CDD) framework for **Java**. Effortlessly translate between **OpenAPI (up to 3.2.0)**, **Arazzo (up to 1.1.0)**, **Model Context Protocol (MCP)**, and native **Java** — generating SDKs, CLI tools, servers, ORM models, tests, and documentation, or extracting specifications back from your existing source code.

---

## Architecture & Vision

**Compiler Driven Development (CDD)** treats specifications and source code as two concrete syntaxes for the same underlying truth. Instead of one-way code generation that overwrites developer customizations, `cdd-java` is built as a bidirectional compiler around a shared Intermediate Representation (IR):

```text
                       ┌──────────────────────────────────────┐
                       │    OpenAPI 3.2.0 / Swagger 2.0       │
                       │    Arazzo 1.1.0  / MCP 1.0.0         │
                       │    JSON Schema   / Docs JSON         │
                       └───────────┬──────────────▲───────────┘
                                   │              │
                            Parse  │              │  Emit
                                   ▼              │
┌────────────────────┐      ┌────────────────────────────┐      ┌────────────────────┐
│  Existing Source   │─────►│   Unified Intermediate     │─────►│  Generated Target  │
│  Code & Docstrings │Parse │   Representation (IR)      │ Emit │  Code & Artifacts  │
│  (AST / Metadata)  │◄─────│ • Endpoints & Operations   │◄─────│ • Client SDKs      │
└────────────────────┘ Emit │ • Schemas & Types          │Parse │ • SDK CLIs         │
                            │ • Security Schemes         │      │ • API Servers      │
                            │ • Arazzo Workflows & Steps │      │ • ORM / Models     │
                            │ • Docstrings & Annotations │      │ • Tests & Mocks    │
                            └────────────────────────────┘      └────────────────────┘
```

### Core Capabilities

1. **Bidirectional OpenAPI 3.2.0 & Swagger 2.0 (`from_openapi` ⇄ `to_openapi`)**
   - Parse OpenAPI 3.0 / 3.1 / 3.2 and Swagger 2.0 (JSON or YAML) into a strongly typed IR.
   - Emit idiomatic Java Client SDKs, CLI wrappers, and server routes/models.
   - Extract OpenAPI 3.2.0 specifications directly from native Java ASTs, type signatures, route decorators/annotations, and structured docstrings.

2. **Bidirectional Arazzo 1.1.0 Workflows (`from_arazzo` ⇄ `to_arazzo`)**
   - Parse multi-step API workflows (`sourceDescriptions`, `workflows`, `steps`, `inputs`, `outputs`, `successCriteria`, `onSuccess`, `onFailure`, `$components`, and runtime expressions like `$steps.<name>.outputs.<field>`).
   - Emit executable Java workflow runners, SDK workflow methods, CLI subcommands, and stateful workflow mock/test harnesses.
   - Reverse-parse workflow orchestration code back into compliant Arazzo 1.1.0 specifications.

3. **In-Place Project Synchronization (`sync`)**
   - AST-aware surgical updates that keep models, routes, docstrings, tests, and OpenAPI/Arazzo specs in lockstep without clobbering handcrafted business logic.

4. **Model Context Protocol (`mcp` & `serve_json_rpc`)**
   - Expose both the `cdd-java` compiler itself and generated API clients/servers as MCP tools and JSON-RPC 2.0 services over `stdio` or HTTP.

5. **Polyglot Documentation Generation (`to_docs_json`)**
   - Emit structured documentation JSON containing executable Java SDK usage snippets for every operation in an OpenAPI spec.

---

## Conformance Matrices

Detailed feature-by-feature conformance tables are maintained in [`conformance/`](./conformance/) (and summarized in [`COMPLIANCE.md`](./COMPLIANCE.md)):

| Specification | Version | Client SDK | Client SDK CLI | Servers (Routes, Models, Tests, Mocks) |
| :--- | :---: | :---: | :---: | :---: |
| **OpenAPI** | `3.2.0` | [`conformance/openapi-3.2.0/client-sdk.md`](./conformance/openapi-3.2.0/client-sdk.md) | [`conformance/openapi-3.2.0/client-sdk-cli.md`](./conformance/openapi-3.2.0/client-sdk-cli.md) | [`conformance/openapi-3.2.0/servers.md`](./conformance/openapi-3.2.0/servers.md) |
| **Arazzo** | `1.1.0` | [`conformance/arazzo-1.1.0/client-sdk.md`](./conformance/arazzo-1.1.0/client-sdk.md) | [`conformance/arazzo-1.1.0/client-sdk-cli.md`](./conformance/arazzo-1.1.0/client-sdk-cli.md) | [`conformance/arazzo-1.1.0/servers.md`](./conformance/arazzo-1.1.0/servers.md) |
| **MCP** | `1.0.0` | [`conformance/mcp-1.0.0/client-sdk.md`](./conformance/mcp-1.0.0/client-sdk.md) | [`conformance/mcp-1.0.0/client-sdk-cli.md`](./conformance/mcp-1.0.0/client-sdk-cli.md) | [`conformance/mcp-1.0.0/servers.md`](./conformance/mcp-1.0.0/servers.md) |
| **Swagger** | `2.0` | [`conformance/swagger-2.0/client-sdk.md`](./conformance/swagger-2.0/client-sdk.md) | [`conformance/swagger-2.0/client-sdk-cli.md`](./conformance/swagger-2.0/client-sdk-cli.md) | [`conformance/swagger-2.0/servers.md`](./conformance/swagger-2.0/servers.md) |

---

## Usage Examples

### 1. Programmatic SDK / Library Usage

```java
import arazzo.Arazzo;
import arazzo.RuntimeExpression;
import cli.CddCli;
import java.util.Map;
import openapi.OpenAPI;

public class Example {
    public static void main(String[] args) throws Exception {
        // 1. Parse an OpenAPI 3.2.0 specification (JSON or YAML) into the IR
        String openApiYaml = "openapi: 3.2.0
"
                + "info:
"
                + "  title: Petstore
"
                + "  version: 1.0.0
"
                + "paths:
"
                + "  /pets/{id}:
"
                + "    get:
"
                + "      operationId: getPetById
"
                + "      summary: Retrieve a pet by ID
"
                + "      parameters:
"
                + "        - name: id
"
                + "          in: path
"
                + "          required: true
"
                + "          schema:
"
                + "            type: string
"
                + "      responses:
"
                + "        '200':
"
                + "          description: OK
"
                + "components:
"
                + "  schemas:
"
                + "    Pet:
"
                + "      type: object
"
                + "      properties:
"
                + "        id:
"
                + "          type: string
"
                + "        name:
"
                + "          type: string
";

        OpenAPI api = openapi.Parse.fromString(openApiYaml);

        // 2. Emit Java Client SDK (models + HttpClient + embedded MCP adapter)
        String sdkSource = classes.Emit.emit(api, null);

        // 3. Emit lexical-preserving HttpClient routes or modular server artifacts
        String routesSource = routes.Emit.emit(api, null);
        Map<String, String> ormEntities = orm.Emit.emitModular(api);
        Map<String, String> daoLayers = dao.Emit.emitModular(api);
        Map<String, String> javalinRoutes = serverroutes.Emit.emitModular(api);

        // 4. Reverse-parse Java AST back into an OpenAPI 3.2.0 model and serialize to JSON
        OpenAPI extractedApi = classes.Parse.parse(sdkSource);
        String emittedOpenApiJson = openapi.Emit.toString(extractedApi);

        // 5. Parse an Arazzo 1.1.0 workflow specification and emit workflow orchestrators
        String arazzoYaml = "arazzo: 1.1.0
"
                + "info:
"
                + "  title: Pet Checkout Workflow
"
                + "  version: 1.0.0
"
                + "sourceDescriptions:
"
                + "  - name: petstore
"
                + "    url: ./openapi.yaml
"
                + "    type: openapi
"
                + "workflows:
"
                + "  - workflowId: adoptPet
"
                + "    steps:
"
                + "      - stepId: fetchPet
"
                + "        operationId: getPetById
";

        Arazzo arazzoSpec = arazzo.Parse.fromString(arazzoYaml);
        String workflowSdk = workflows.Emit.emit(arazzoSpec);
        String workflowCli = cli.ArazzoCliEmitter.emitWorkflowCli(arazzoSpec);
        String workflowTests = tests.ArazzoTestEmitter.emit(arazzoSpec);
        String workflowMocks = mocks.ArazzoMockEmitter.emit(arazzoSpec);

        // 6. Evaluate Arazzo 1.1.0 runtime expressions directly
        RuntimeExpression.Context ctx = new RuntimeExpression.Context();
        ctx.statusCode = 200;
        ctx.inputs.put("petId", "pet-42");
        Object petId = RuntimeExpression.evaluate("$inputs.petId", ctx);

        // 7. Invoke the CLI programmatically
        CddCli.generateFromOpenApi("to_sdk", "openapi.yaml", "./generated-sdk", false, false, true);
        CddCli.generateFromArazzo("to_sdk", "arazzo.yaml", "./generated-workflows", false, false, true);
    }
}
```

### 2. Command-Line Quickstart

```sh
# Generate a Java Client SDK from an OpenAPI 3.2.0 spec (with unit tests)
cdd-java from_openapi to_sdk -i openapi.yaml -o ./sdk --tests

# Generate a runnable CLI application from an OpenAPI spec
cdd-java from_openapi to_sdk_cli -i openapi.yaml -o ./cli

# Generate a server (routes, models, ORM, mocks, tests) from an OpenAPI spec
cdd-java from_openapi to_server -i openapi.yaml -o ./server

# Reverse-engineer an OpenAPI 3.2.0 spec from existing Java source code
cdd-java to_openapi -i ./src -o openapi.json

# Generate workflow SDK, CLI, or server tests from an Arazzo 1.1.0 spec
cdd-java from_arazzo to_sdk -i arazzo.yaml -o ./workflows --tests
cdd-java from_arazzo to_sdk_cli -i arazzo.yaml -o ./workflows-cli
cdd-java from_arazzo to_server -i arazzo.yaml -o ./workflows-server

# Extract an Arazzo 1.1.0 spec from existing workflow code
cdd-java to_arazzo -i ./workflows -o arazzo.json

# Synchronize an existing project directory in-place
cdd-java sync -i ./my-project

# Emit documentation JSON with Java code snippets
cdd-java to_docs_json -i openapi.yaml -o docs.json

# Run as a Model Context Protocol (MCP) server over stdio or JSON-RPC 2.0 server
cdd-java mcp
cdd-java serve_json_rpc --port 8080 --listen 127.0.0.1
```

---

## Installation & Development

### Prerequisites

- **Java JDK**: Java 11 or newer (JDK 11–23 recommended for Maven/JaCoCo test runs).
- **Apache Maven**: `3.8+` (`mvn`).
- **Optional (WebAssembly Build)**: GraalVM 25+ with `wasm32-wasi` native-image support (`GRAALVM_HOME`) and WASI SDK (`WASI_SDK_PATH`), plus Node.js 18+ for the `@offscale/cdd-java` npm package.
- **Optional (Docker)**: `docker`, `nerdctl`, or `lima nerdctl`.

### Installing & Building from Source

```sh
# Clone the repository
git clone https://github.com/offscale/cdd-java.git
cd cdd-java

# Install runtime dependencies into lib/ and compile classes into bin/
make install_deps
make build

# Or build the standalone shaded uber-JAR via Maven (target/cdd-java-0.0.4.jar)
mvn clean package -DskipTests

# Run the CLI via the shaded JAR or Makefile target
java -jar target/cdd-java-0.0.4.jar --help
make run -- --help
```

### Running Tests, Coverage & Quality Gates

```sh
# Run the full JUnit 4 test suite and build the shaded JAR
make test

# Run tests with strict 100% JaCoCo instruction, line, branch, method, and class coverage verification
mvn test jacoco:report jacoco:check

# Run Spotless formatting and strict Javadoc doclint verification
mvn spotless:check javadoc:javadoc

# Verify 100% Javadoc coverage and generate HTML Javadoc
python3 scripts/check_missing_docs.py
make docs
```

### Building WebAssembly & Docker Artifacts

```sh
# Compile cdd-java to WebAssembly (target/wasm/cdd-java.wasm) and build the JS/TS wrapper
make build_wasm

# Build and test Alpine and Debian container images
make build_docker
make run_docker
```

---

## CLI Reference

### Core CLI Subcommands & Flags Summary

| Command / Subcommand | Direction | Primary Flags / Options | Description |
| :--- | :---: | :--- | :--- |
| `from_openapi [to_sdk]` | Spec → Code | `-i <spec>`, `--input-dir <dir>`, `-o <dir>`, `--tests`, `--mcp`, `--no-github-actions`, `--no-installable-package` | Generate a Java Client SDK (models, HTTP client, auth, and optional tests/MCP). |
| `from_openapi to_sdk_cli` | Spec → Code | `-i <spec>`, `--input-dir <dir>`, `-o <dir>`, `--tests`, `--no-github-actions`, `--no-installable-package` | Generate a standalone Java CLI application wrapping the generated Client SDK. |
| `from_openapi to_server` | Spec → Code | `-i <spec>`, `--input-dir <dir>`, `-o <dir>`, `--tests`, `--mcp`, `--no-github-actions`, `--no-installable-package` | Generate server routes, controllers, ORM/models, mock backends, and integration tests. |
| `to_openapi` | Code → Spec | `-i <path>`, `-o <spec.json\|yaml>` | Parse Java source code/AST and emit an OpenAPI 3.2.0 specification. |
| `from_arazzo [to_sdk]` | Spec → Code | `-i <arazzo>`, `-o <dir>`, `--tests`, `--no-github-actions`, `--no-installable-package` | Generate executable Java workflow client orchestrators from an Arazzo 1.1.0 spec. |
| `from_arazzo to_sdk_cli` | Spec → Code | `-i <arazzo>`, `-o <dir>`, `--tests`, `--no-github-actions`, `--no-installable-package` | Generate a CLI workflow runner for Arazzo 1.1.0 workflows. |
| `from_arazzo to_server` | Spec → Code | `-i <arazzo>`, `-o <dir>`, `--tests`, `--no-github-actions`, `--no-installable-package` | Generate server-side workflow E2E tests and stateful workflow mocks. |
| `to_arazzo` | Code → Spec | `-i <path>`, `-o <arazzo.json\|yaml>` | Extract an Arazzo 1.1.0 workflow specification from Java workflow code/tests. |
| `sync` | Code ⇄ Spec | `-i <dir>`, `-o <dir>`, `--truth <type>` | Synchronize code, models, routes, tests, and OpenAPI/Arazzo specs in-place using AST surgery. |
| `to_docs_json` | Spec → Docs | `-i <spec>`, `-o <docs.json>`, `--no-imports`, `--no-wrapping` | Emit documentation JSON containing Java SDK code examples per operation. |
| `serve_json_rpc` | Server | `--port <port>`, `--listen <host>`, `--wasi` | Expose all `cdd-java` compiler commands as a JSON-RPC 2.0 / MCP service. |
| `mcp` | Server | `--port <port>`, `--listen <host>`, `--wasi` | Start a Model Context Protocol (MCP) server over `stdio` or HTTP. |
| `process_in_memory` | In-Memory | `<json_payload>` | Execute compilation in memory from a JSON payload (used by WASI and MCP). |

### Common Flags & Environment Variables

| Flag | Short | Environment Variable | Description |
| :--- | :---: | :--- | :--- |
| `--help` | `-h` | — | Display help information for the CLI or active subcommand. |
| `--version` | `-v`, `-V` | — | Print the `cdd-java` version. |
| `--input <path>` | `-i`, `-f` | `CDD_INPUT` | Path to the input specification file or source code directory. |
| `--input-dir <dir>` | `-d` | `CDD_INPUT_DIR` | Path to a directory containing multiple specification or source files. |
| `--output <path>` | `-o` | `CDD_OUTPUT` | Target output file path or destination directory. |
| `--tests` | `-t` | `CDD_TESTS` | Generate unit, contract, and workflow integration tests alongside code. |
| `--mcp` | `-m` | `CDD_MCP` | Emit Model Context Protocol (MCP) tool/resource adapters alongside SDK/server code. |
| `--no-github-actions` | — | `CDD_NO_GITHUB_ACTIONS` | Skip generating `.github/workflows/ci.yml` in the target project. |
| `--no-installable-package` | — | `CDD_NO_INSTALLABLE_PACKAGE` | Skip generating package manifest/build files in the target project. |
| `--no-imports` | — | `CDD_NO_IMPORTS` | Omit import/module preamble from generated `to_docs_json` snippets. |
| `--no-wrapping` | — | `CDD_NO_WRAPPING` | Omit `main()` / function wrapper boilerplate from `to_docs_json` snippets. |
| `--port <port>` | `-p` | `CDD_PORT` | Port number for `serve_json_rpc` (default: `8080`). |
| `--listen <addr>` | `-l` | `CDD_LISTEN` | Bind address for `serve_json_rpc` (default: `127.0.0.1`). |
| `--truth <source>` | `-t` | `CDD_TRUTH` | Authoritative source of truth during `sync` (`openapi`, `orm`, `routes`, `classes`). |

### Verbatim CLI `--help` Output

#### `cdd-java --help`

```text
cdd-java CLI
Usage:
  cdd-java [subcommand] [options]

Subcommands:
  from_openapi       Generate code from an OpenAPI specification.
  to_openapi         Generate an OpenAPI specification from source code.
  from_arazzo        Generate workflow code from an Arazzo 1.1.0 specification.
  to_arazzo          Generate an Arazzo 1.1.0 specification from workflow source code.
  to_docs_json       Generate JSON documentation with code snippets for an OpenAPI specification.
  serve_json_rpc     Expose CLI interface as a JSON-RPC server.
  mcp                Expose CLI interface as a Model Context Protocol (MCP) server.
  sync               Synchronize database schema to models and OpenAPI specifications.
  process_in_memory  Execute compilation in memory from a JSON payload.

Options:
  --help, -h      Show this help message.
  --version, -v   Show version information.

Examples:
  cdd-java serve_json_rpc [--port 8080] [--listen 127.0.0.1] [--wasi]
  cdd-java mcp [--port 8080] [--listen 127.0.0.1] [--wasi]
  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]
  cdd-java from_arazzo to_sdk_cli -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_arazzo to_sdk -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_arazzo to_server -i <arazzo.json> [-o <target_directory>]
  cdd-java to_openapi -i <path/to/code> [-o <spec.json>]
  cdd-java to_arazzo -i <path/to/code> [-o <arazzo.json>]
  cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json> [-o <docs.json>]
  cdd-java sync -i <dir> [-o <dir>] [--truth <type>]
```

#### `cdd-java from_openapi --help`

```text
cdd-java from_openapi
Usage:
  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]
```

#### `cdd-java from_openapi to_sdk --help`

```text
cdd-java from_openapi
Usage:
  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]
```

#### `cdd-java from_openapi to_sdk_cli --help`

```text
cdd-java from_openapi
Usage:
  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]
```

#### `cdd-java from_openapi to_server --help`

```text
cdd-java from_openapi
Usage:
  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]
```

#### `cdd-java to_openapi --help`

```text
cdd-java to_openapi
Usage:
  cdd-java to_openapi -i <path/to/code> [-o <spec.json>]
```

#### `cdd-java from_arazzo --help`

```text
cdd-java from_arazzo
Usage:
  cdd-java from_arazzo to_sdk_cli -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_arazzo to_sdk -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_arazzo to_server -i <arazzo.json> [-o <target_directory>]
```

#### `cdd-java from_arazzo to_sdk --help`

```text
cdd-java from_arazzo
Usage:
  cdd-java from_arazzo to_sdk_cli -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_arazzo to_sdk -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_arazzo to_server -i <arazzo.json> [-o <target_directory>]
```

#### `cdd-java from_arazzo to_sdk_cli --help`

```text
cdd-java from_arazzo
Usage:
  cdd-java from_arazzo to_sdk_cli -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_arazzo to_sdk -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_arazzo to_server -i <arazzo.json> [-o <target_directory>]
```

#### `cdd-java from_arazzo to_server --help`

```text
cdd-java from_arazzo
Usage:
  cdd-java from_arazzo to_sdk_cli -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_arazzo to_sdk -i <arazzo.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package] [--tests]
  cdd-java from_arazzo to_server -i <arazzo.json> [-o <target_directory>]
```

#### `cdd-java to_arazzo --help`

```text
cdd-java to_arazzo
Usage:
  cdd-java to_arazzo -i <path/to/code> [-o <arazzo.json>]
```

#### `cdd-java sync --help`

```text
cdd-java sync
Usage:
  cdd-java sync -i <dir> [-o <dir>] [--truth <type>]
```

#### `cdd-java to_docs_json --help`

```text
cdd-java to_docs_json
Usage:
  cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json> [-o <docs.json>]
```

#### `cdd-java serve_json_rpc --help`

```text
cdd-java serve_json_rpc
Usage:
  cdd-java serve_json_rpc [-p|--port <port>] [-l|--listen <address>] [--wasi]
```

#### `cdd-java mcp --help`

```text
cdd-java mcp
Usage:
  cdd-java mcp [-p|--port <port>] [-l|--listen <address>] [--wasi]
```

#### `cdd-java process_in_memory --help`

```text
cdd-java process_in_memory
Usage:
  cdd-java process_in_memory <json_payload>
```

---

## Special Features Beyond the Common Compiler Driven Development Subset

### 1. Lexical-Preserving AST Round-Trip Engine (`JavaParser` + `LexicalPreservingPrinter`)
- Every emitter in `cdd-java` (`classes.Emit`, `routes.Emit`, `orm.Emit`, `mocks.Emit`, `tests.Emit`, `functions.Emit`, and `workflows.Emit`) integrates `com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter`.
- When updating existing `.java` source files via `sync` or programmatic `emit(model, existingSource)`, `cdd-java` parses the existing compilation unit, attaches the lexical-preserving printer, and surgically inserts or updates missing fields, methods, annotations, and Javadoc tags while preserving original indentation, comments, imports, and custom developer method bodies.
- OpenAPI 3.2.0 and Arazzo 1.1.0 metadata that has no native Java syntax equivalent (such as `@openapiVersion`, `@openapiSelf`, `@jsonSchemaDialect`, `@xmlName`, `@xmlNamespace`, `@xmlPrefix`, `@xmlAttribute`, `@xmlWrapped`, `@discriminatorProperty`, `@discriminatorMapping`, `@discriminatorDefault`, `@callback`, `@pathRef`, `@requestBodyContent`, `@responseContent`, `@arazzoSelf`, `@sourceDescription`, `@workflowId`, `@stepMeta`, and `@stepCriterion`) is round-tripped losslessly through structured Javadoc block tags.

### 2. Full-Stack Modular Server Generation (Javalin 5 + Hibernate 6 / Jakarta Persistence + DataFaker)
Running `cdd-java from_openapi to_server -i <spec> -o <dir>` scaffolds a complete, runnable multi-package Java backend supporting decoupled CDD server modes and advanced mock capabilities:
- **Decoupled CDD Server Modes (`servermain.Emit`, `mocks/DbConfig.java`, `mocks/DaoFactory.java`)**:
  - `start` (No DB configured): **Stub Mode**. Server runs using traditional scaffolds (`Stub<Schema>Dao`), endpoints return `UnsupportedOperationException` / empty bodies.
  - `start` (With `DATABASE_URL`): **Production Mode**. Uses actual Hibernate 6 / Jakarta Persistence ORM interactions (`Concrete<Schema>Dao`) against a real database.
  - `start --ephemeral` (or `-e` / `EPHEMERAL_DB=true`): **Sandbox Mode**. Uses actual ORM interactions against a fresh, throwaway SQLite in-memory database (`jdbc:sqlite::memory:`).
  - `start --ephemeral --seed` (or `-e -s` / `SEED_DB=1`): **Full Mock Mode**. Ephemeral database automatically populated with a topologically sorted, localized fake data graph via `Seeder.seedDatabase(...)`.
- **Advanced Mock Capabilities (`routes/MiddlewareRoutes.java`)**:
  - **Strict Request Validation (`--strict-validation`)**: Intercepts incoming `POST`/`PUT` requests and validates constraints against the OpenAPI/MCP schema, returning detailed `400 Bad Request` JSON payloads (e.g., `"Field 'email' must match format 'email'"`, `"Array 'items' exceeds maximum length"`).
  - **Hybrid Authentication (`--enforce-auth`)**: Enforces Bearer token (`Bearer mock-token-123`), API key (`X-API-Key: mock-api-key-123`), and IdP session validation (or stateful `User` ORM lookup in Production Mode), returning `401 Unauthorized` when missing or `403 Forbidden` when invalid.
  - **Integrated Identity Provider (`--start-auth-server`) & SDK Auth (`client.auth`)**: Mounts `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, and `POST /auth/logout` endpoints alongside the generated `AuthAdapter` (`client.auth.register`, `client.auth.login`, `client.auth.refresh`, `client.auth.logout`) on the Client SDK.
  - **Administrative Webhook Trigger (`POST /_mock/trigger-webhook/{webhook_name}`)**: Dispatches outgoing webhook/callback payloads (`{"targetUrl": "...", "payload": {...}}`) over `java.net.http.HttpClient` with `X-Webhook-Name` headers.
- **Javalin HTTP Routes & Server Entrypoint (`serverroutes.Emit`, `servermain.Emit`)**: Generates `routes/<Resource>Routes.java` with CRUD handlers (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`) and `servermain/Main.java` with CORS enabled on port `8080`.
- **Hibernate 6 / Jakarta Persistence ORM Entities (`orm.Emit`, `orm.Parse`)**: Bidirectional generation and AST extraction of `@Entity`, `@Table`, `@Id`, `@Column`, `@OneToMany`, and `@ManyToOne` classes under `models/<Schema>.java`.
- **Dual-Mode DAO Layer (`dao.Emit`, `mocks.Emit`)**: Generates typed DAO interfaces alongside both `Concrete<Schema>Dao` (backed by Jakarta `EntityManager` transactions) and `Stub<Schema>Dao`, wired through `mocks/DaoFactory.java` and CLI/environment-driven `mocks/DbConfig.java`.
- **Automated Synthetic Database Seeder (`seeder.Emit`)**: Generates `seeder/Seeder.java` powered by `net.datafaker.Faker` to populate realistic domain entities automatically when `--seed` or `SEED_DB` is set.
- **Server Test Suites (`servertests.Emit`)**: Emits JUnit 4 model instantiation tests (`models/<Schema>Test.java`), route/DAO wiring tests (`routes/<Resource>RoutesTest.java`), and middleware integration tests (`routes/MiddlewareRoutesTest.java`).

### 3. Embedded Model Context Protocol (MCP) Client & SSE Adapter (`classes.McpEmitter` & `routes.Emit`)
- Every generated Java SDK client (`<Title>Client`) embeds an inner `McpAdapter` (`client.mcp`) along with complete Jackson-annotated MCP 1.0.0 protocol POJOs (`Tool`, `Resource`, `CallToolRequest`, `CallToolResult`, `InitializeRequest`, `InitializeResult`, `Prompt`, `SamplingMessage`, `CompleteRequest`, `Root`, etc.).
- Provides `client.mcp.getTools()` (exposing all OpenAPI operations with JSON Schema `inputSchema`), `client.mcp.callTool(name, arguments)` (invoking the underlying `java.net.http.HttpClient` operation and wrapping the HTTP response in `CallToolResult`), `client.mcp.getResources()` (`cdd://openapi/spec`), and `client.mcpSse(toolName, arguments, headers)` for remote Server-Sent Events (SSE) tool dispatch with automatic `Authorization` header propagation.

### 4. MCP & JSON-RPC 2.0 Compiler Server with Live AST Resources (`mcp` / `serve_json_rpc`)
- `cdd-java mcp` and `cdd-java serve_json_rpc` expose the compiler over both HTTP (`com.sun.net.httpserver.HttpServer`) and STDIO (`--wasi`).
- Supports MCP methods `initialize` (protocol `2024-11-05`), `ping`, `version`, `tools/list` (`cdd_generate`), `tools/call`, `resources/list`, and `resources/read`.
- Exposes live workspace AST inspection resources: reading `cdd://ast/openapi` dynamically parses the workspace into an OpenAPI 3.2.0 specification, and reading `cdd://ast/arazzo` dynamically extracts the workspace's Arazzo 1.1.0 workflow specification.

### 5. Complete Bidirectional Arazzo 1.1.0 Workflow Engine (`arazzo.*`, `workflows.*`)
- **Runtime Expression Evaluator (`arazzo.RuntimeExpression`)**: Evaluates and validates Arazzo 1.1.0 expressions including `$url`, `$method`, `$statusCode`, `$request.header.<name>`, `$request.query.<name>`, `$request.path.<name>`, `$request.body#<json-pointer>`, `$response.header.<name>`, `$response.body#<json-pointer>`, `$inputs.<name>`, `$outputs.<name>`, `$steps.<stepId>.outputs.<name>`, `$workflows.<workflowId>.outputs.<name>`, `$sourceDescriptions.<name>.url`, `$components.parameters.<name>`, and string-interpolated `{expression}` templates.
- **Cross-Spec Resolution & Topological Sorting (`arazzo.ArazzoResolver`)**: Loads linked OpenAPI `sourceDescriptions`, resolves `$components` reusable `parameters`, `successActions`, `failureActions`, and `inputs`, and topologically sorts workflows with `dependsOn` dependencies.
- **Four Bidirectional Targets**: Emits and reverse-parses SDK orchestrators (`workflows.Emit`/`Parse`), standalone CLI runners (`cli.ArazzoCliEmitter`/`ArazzoCliParser`), JUnit 4 E2E tests (`tests.ArazzoTestEmitter`/`ArazzoTestParser`), and stateful retry-aware workflow simulators (`mocks.ArazzoMockEmitter`/`ArazzoMockParser`).

### 6. In-Memory Compilation & GraalVM WebAssembly (`process_in_memory` & `@offscale/cdd-java`)
- **In-Memory Pipeline (`cdd-java process_in_memory <json_payload>`)**: Accepts a JSON object containing `{ "command": [...], "files": { ... } }` and returns generated files in a JSON envelope delimited by `CDD_IN_MEMORY_START` and `CDD_IN_MEMORY_END` without touching the host filesystem.
- **WASI Virtual Root & JS/TS SDK**: Supports `CDD_WASI_VIRTUAL_ROOT` path remapping and WASI `_start` entrypoints (`cli.CddCli._start`), compiled via GraalVM `native-image --tool:svm-wasm` (`build_wasm.py`) and wrapped for Node.js and browser Web Workers in `package/` (`@offscale/cdd-java`).

### 7. Selective Source-of-Truth Synchronization (`sync --truth <type>`)
- The `sync` subcommand supports `--truth openapi` (prioritizing `openapi.json`), `--truth orm` (deriving schemas exclusively from Jakarta Persistence `@Entity` classes), `--truth classes` (deriving schemas from Jackson POJOs), or full multi-package aggregation across `classes`, `orm`, `routes`, `mocks`, `tests`, `functions`, `cli`, and `workflows`.

---

## Companion Documentation

- [`ARCHITECTURE.md`](./ARCHITECTURE.md) — Internal compiler pipeline, AST parsers/emitters, and intermediate representation design.
- [`USAGE.md`](./USAGE.md) — Extended CLI workflows, library integration examples, and configuration recipes.
- [`COMPLIANCE.md`](./COMPLIANCE.md) — OpenAPI 3.2.0, Arazzo 1.1.0, MCP 1.0.0, and Swagger 2.0 compliance status.
- [`DEVELOPING.md`](./DEVELOPING.md) — Local build instructions, test runner usage, linter configuration, and contribution guidelines.
- [`WASM.md`](./WASM.md) — WebAssembly / WASI build pipeline and browser integration guide.
- [`PUBLISH.md`](./PUBLISH.md) — Release checklists and package registry publishing instructions.

---

## License

Licensed under either of

- Apache License, Version 2.0 ([LICENSE-APACHE](./LICENSE-APACHE) or <https://www.apache.org/licenses/LICENSE-2.0>)
- MIT license ([LICENSE-MIT](./LICENSE-MIT) or <https://opensource.org/licenses/MIT>)

at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall be
dual licensed as above, without any additional terms or conditions.
