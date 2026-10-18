# Compliance

`cdd-java` strives for 100% compliance with the [OpenAPI Specification 3.2.0](https://raw.githubusercontent.com/OAI/OpenAPI-Specification/refs/heads/main/versions/3.2.0.md).

## Current Status

- **OpenAPI 3.0.x**: Fully supported (Parsing and Emitting).
- **OpenAPI 3.1.0**: Fully supported.
- **Swagger 2.0 and OpenAPI 3.2.0**: Fully supported (100% compliance achieved for parsing and emitting the latest draft structures).
- **Arazzo 1.1.0**: Fully supported bidirectionally (`from_arazzo` and `to_arazzo` across Client SDK workflow orchestrators, Client SDK CLI workflow runners, Server E2E integration tests, and stateful workflow mocks).

If you find discrepancies with the OpenAPI specification, please file an issue or submit a pull request with an AST-based parser adjustment.

## Support Matrix

| Specification Concept       | Status |
| --------------------------- | ------ |
| Info Object                 | ✅     |
| Server Object               | ✅     |
| Components Object           | ✅     |
| Paths Object                | ✅     |
| Path Item Object            | ✅     |
| Operation Object            | ✅     |
| Parameter Object            | ✅     |
| Request Body Object         | ✅     |
| Responses Object            | ✅     |
| Responses / Schema Object   | ✅     |
| Reference Object (`$ref`)   | ✅     |
| OAuth Flows                 | ✅     |
| Security Requirement Object | ✅     |
| Links / Callbacks           | ✅     |
| Webhooks                    | ✅     |
