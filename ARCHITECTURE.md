# `cdd-java` Architecture

[![License](https://img.shields.io/badge/license-Apache--2.0%20OR%20MIT-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![CI/CD](https://github.com/offscale/cdd-java/workflows/CI/badge.svg)](https://github.com/offscale/cdd-java/actions)
[![Test Coverage](https://img.shields.io/badge/coverage-100%25-brightgreen.svg)](https://github.com/offscale/cdd-java/actions)
[![Doc Coverage](https://img.shields.io/badge/docs-100%25-brightgreen.svg)](https://offscale.github.io/cdd-java/)

`cdd-java` is a bidirectional Compiler Driven Development (CDD) engine for Java, translating between **OpenAPI 3.2.0 / Swagger 2.0**, **Arazzo 1.1.0**, **Model Context Protocol (MCP) 1.0.0**, and Java source code ASTs via `com.github.javaparser`.

---

## Package Architecture (`src/main/java/`)

| Package | Role | Key Classes |
| :--- | :--- | :--- |
| `openapi` | OpenAPI 3.2.0 / Swagger 2.0 IR, parser, serializer, `$ref` resolver, and Java type mapper | `OpenAPI`, `Parse`, `Emit`, `RefResolver`, `TypeMapper`, `Schema`, `Paths`, `PathItem`, `Operation`, `Components`, `SecurityScheme`, etc. |
| `arazzo` | Arazzo 1.1.0 IR, parser, serializer, cross-spec resolver, and runtime expression evaluator | `Arazzo`, `Parse`, `Emit`, `ArazzoResolver`, `RuntimeExpression`, `Workflow`, `Step`, `Criterion`, `SuccessAction`, `FailureAction`, `ArazzoComponents`, etc. |
| `classes` | Jackson model POJOs, embedded SDK HTTP client, and MCP 1.0.0 schema adapter | `Emit`, `Parse`, `McpEmitter` |
| `routes` | Lexical-preserving `java.net.http.HttpClient` route methods, SSE MCP adapter (`mcpSse`), webhook & callback interfaces | `Emit`, `Parse` |
| `orm` | Jakarta Persistence (`jakarta.persistence.*`) / Hibernate 6 `@Entity` classes | `Emit`, `Parse` |
| `dao` | Data Access Object interfaces, `Concrete<Entity>Dao`, and `Stub<Entity>Dao` implementations | `Emit` |
| `seeder` | Synthetic database seeding via `net.datafaker.Faker` | `Emit` |
| `serverroutes` | Modular Javalin 5 HTTP route controllers (`<Resource>Routes.java`) | `Emit` |
| `servermain` | Modular Javalin 5 server entrypoint (`Main.java`) | `Emit` |
| `servertests` | Modular JUnit 4 model and route/DAO wiring test suites | `Emit` |
| `workflows` | Bidirectional Arazzo 1.1.0 Java SDK workflow orchestrators | `Emit`, `Parse` |
| `cli` | Compiler CLI (`Main`, `CddCli`), generated OpenAPI SDK CLI (`Emit`, `Parse`), and Arazzo workflow CLI (`ArazzoCliEmitter`, `ArazzoCliParser`) | `Main`, `CddCli`, `Emit`, `Parse`, `ArazzoCliEmitter`, `ArazzoCliParser` |
| `tests` | Bidirectional OpenAPI JUnit 4 integration tests (`Emit`, `Parse`) and Arazzo E2E workflow tests (`ArazzoTestEmitter`, `ArazzoTestParser`) | `Emit`, `Parse`, `ArazzoTestEmitter`, `ArazzoTestParser` |
| `mocks` | Bidirectional OpenAPI mock HTTP servers (`Emit`, `Parse`) and Arazzo stateful workflow simulators (`ArazzoMockEmitter`, `ArazzoMockParser`) | `Emit`, `Parse`, `ArazzoMockEmitter`, `ArazzoMockParser` |
| `functions` | Lexical-preserving helper function generation and extraction | `Emit`, `Parse` |
| `docstrings` | Documentation JSON generator (`to_docs_json`) and Javadoc tag parser | `Emit`, `Parse` |

---

## Core Design Principles

1. **Zero Data Loss Round-Trips**: Every OpenAPI 3.2.0 and Arazzo 1.1.0 field that lacks a direct Java AST construct is preserved in structured Javadoc tags and reconstructed by the corresponding `Parse` class.
2. **Lexical Preservation**: All AST emitters attach `LexicalPreservingPrinter` when modifying existing `.java` files so developer formatting, comments, and custom method implementations remain untouched.
3. **Symmetric Compilation**: Every emitter (`*.Emit`) has a corresponding reverse parser (`*.Parse`) operating over the same intermediate representation (`openapi.OpenAPI` and `arazzo.Arazzo`).
4. **Transport Agnostic Execution**: The compiler runs natively on the JVM, as an HTTP or STDIO JSON-RPC 2.0 / MCP server, or in-memory inside GraalVM WebAssembly (`wasm32-wasi`).
