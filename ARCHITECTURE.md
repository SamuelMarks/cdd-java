# Architecture

`cdd-java` is built on a bidirectional Abstract Syntax Tree (AST) architecture.

1. **Parser Phase**: Reads Java or OpenAPI files to construct an intermediate `openapi.*` model.
2. **Emitter Phase**: Uses the `openapi.*` models to emit output targets: OpenAPI JSON or Java Source Code.

## Package Structure

- `openapi`: Primitive OpenAPI 3.2.0 representations.
- `classes`, `routes`, `functions`, `cli`, `tests`: Modules handling specific subsets of conversions.
- `cli.Main`: The main entrypoint, routing command-line flags to specific emitter workflows.
- `com.github.javaparser`: Core third-party dependency providing `Java` lexing/parsing.
- `com.fasterxml.jackson`: Dependency used for reading/writing JSON and YAML with type-safety.

## Goals

1. **No External Runtime Requirements**: Generates native `Java` that requires minimal dependencies.
2. **Bidirectionality**: Generating a model and then modifying it generates the reverse result deterministically.
