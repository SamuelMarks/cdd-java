cdd-LANGUAGE
============

[![License](https://img.shields.io/badge/license-Apache--2.0%20OR%20MIT-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![CI/CD](https://github.com/offscale/cdd-java/workflows/CI/badge.svg)](https://github.com/offscale/cdd-java/actions)
[![Test Coverage](https://img.shields.io/badge/Coverage-100%25-success.svg)](#)
[![Doc Coverage](https://img.shields.io/badge/Doc%20Coverage-100%25-success.svg)](#)

OpenAPI ↔ Java. This is one compiler in a suite, all focussed on the same task: Compiler Driven Development (CDD).

Each compiler is written in its target language, is whitespace and comment sensitive, and has both an SDK and CLI.

The CLI—at a minimum—has:

```text
cdd-java CLI
Usage:
  cdd-java --help
  cdd-java --version
  cdd-java serve_json_rpc [--port <port>] [--listen <ip>]
  cdd-java from_openapi to_sdk_cli -i <spec.json> [-o <target_directory>] [--no-github-actions] [--no-installable-package]
  cdd-java from_openapi to_sdk -i <spec.json> [-o <target_directory>]
  cdd-java from_openapi to_server -i <spec.json> [-o <target_directory>]
  cdd-java to_openapi -f <path/to/code> [-o <spec.json>]
  cdd-java to_docs_json [--no-imports] [--no-wrapping] -i <spec.json> [-o <docs.json>]
```

The goal of this project is to enable rapid application development without tradeoffs. Tradeoffs of Protocol Buffers / Thrift etc. are an untouchable "generated" directory and package, compile-time and/or runtime overhead. Tradeoffs of Java or JavaScript for everything are: overhead in hardware access, offline mode, ML inefficiency, and more. And neither of these alterantive approaches are truly integrated into your target system, test frameworks, and bigger abstractions you build in your app. Tradeoffs in CDD are code duplication (but CDD handles the synchronisation for you).

## 🚀 Capabilities

The `cdd-java` compiler leverages a unified architecture to support various facets of API and code lifecycle management.

* **Compilation**:
  * **OpenAPI → `Java`**: Generate idiomatic native models, network routes, client SDKs, database schemas, and boilerplate directly from OpenAPI (`.json` / `.yaml`) specifications.
  * **`Java` → OpenAPI**: Statically parse existing `Java` source code and emit compliant OpenAPI specifications.
* **AST-Driven & Safe**: Employs static analysis (Abstract Syntax Trees) instead of unsafe dynamic execution or reflection, allowing it to safely parse and emit code even for incomplete or un-compilable project states.
* **Seamless Sync**: Keep your docs, tests, database, clients, and routing in perfect harmony. Update your code, and generate the docs; or update the docs, and generate the code.

## 📦 Installation

<!-- INSTRUCTION TO LLM: Insert specific installation instructions, package managers, and prerequisites here based on the `Requires Java 11+. Clone the repo and run `make install_base && make install_deps`.` context. -->

## 🛠 Usage

### Command Line Interface

<!-- INSTRUCTION TO LLM: Provide 1-2 idiomatic CLI examples using the `make run` placeholder. Ensure paths reflect standard `Java` project structures. -->

### Programmatic SDK / Library

<!-- INSTRUCTION TO LLM: Provide a small code snippet in `Java` demonstrating how to invoke the compiler as a library, using the `java`. -->

## Design choices

The compiler is built using `javaparser-core` for robust AST-driven extraction of Java classes and routes without the need to compile them first. It avoids reflection entirely. Jackson is used for parsing and writing JSON specs.

## 🏗 Supported Conversions for Java

*(The boxes below reflect the features supported by this specific `cdd-java` implementation)*

| Concept | Parse (From) | Emit (To) |
|---------|--------------|-----------|
| OpenAPI (JSON/YAML) | ✅ | ✅ |
| `Java` Models / Structs / Types | [ ] | [ ] |
| `Java` Server Routes / Endpoints | [ ] | [ ] |
| `Java` API Clients / SDKs | [ ] | [ ] |
| `Java` ORM / DB Schemas | [ ] | [ ] |
| `Java` CLI Argument Parsers | [ ] | [ ] |
| `Java` Docstrings / Comments | [ ] | [ ] |

<!-- INSTRUCTION TO LLM: Check the boxes above (`✅`) based on the `Parse OpenAPI, Emit Classes, Emit Routes, Parse Classes, Parse Routes` context provided. -->

---

## License

Licensed under either of

- Apache License, Version 2.0 ([LICENSE-APACHE](LICENSE-APACHE) or <https://www.apache.org/licenses/LICENSE-2.0>)
- MIT license ([LICENSE-MIT](LICENSE-MIT) or <https://opensource.org/licenses/MIT>)

at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall be
dual licensed as above, without any additional terms or conditions.