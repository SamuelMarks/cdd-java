# Compliance

`cdd-java` provides bidirectional compliance across **OpenAPI 3.2.0**, **Arazzo 1.1.0**, **Model Context Protocol (MCP) 1.0.0**, and **Swagger 2.0**.

---

## Conformance Matrices

Detailed feature-by-feature conformance tables are maintained in [`conformance/`](./conformance/):

| Specification | Version | Client SDK | Client SDK CLI | Servers (Routes, Models, Tests, Mocks) |
| :--- | :---: | :---: | :---: | :---: |
| **OpenAPI** | `3.2.0` | [`conformance/openapi-3.2.0/client-sdk.md`](./conformance/openapi-3.2.0/client-sdk.md) | [`conformance/openapi-3.2.0/client-sdk-cli.md`](./conformance/openapi-3.2.0/client-sdk-cli.md) | [`conformance/openapi-3.2.0/servers.md`](./conformance/openapi-3.2.0/servers.md) |
| **Arazzo** | `1.1.0` | [`conformance/arazzo-1.1.0/client-sdk.md`](./conformance/arazzo-1.1.0/client-sdk.md) | [`conformance/arazzo-1.1.0/client-sdk-cli.md`](./conformance/arazzo-1.1.0/client-sdk-cli.md) | [`conformance/arazzo-1.1.0/servers.md`](./conformance/arazzo-1.1.0/servers.md) |
| **MCP** | `1.0.0` | [`conformance/mcp-1.0.0/client-sdk.md`](./conformance/mcp-1.0.0/client-sdk.md) | [`conformance/mcp-1.0.0/client-sdk-cli.md`](./conformance/mcp-1.0.0/client-sdk-cli.md) | [`conformance/mcp-1.0.0/servers.md`](./conformance/mcp-1.0.0/servers.md) |
| **Swagger** | `2.0` | [`conformance/swagger-2.0/client-sdk.md`](./conformance/swagger-2.0/client-sdk.md) | [`conformance/swagger-2.0/client-sdk-cli.md`](./conformance/swagger-2.0/client-sdk-cli.md) | [`conformance/swagger-2.0/servers.md`](./conformance/swagger-2.0/servers.md) |

---

## Specification Summary

### OpenAPI 3.2.0 & Swagger 2.0

| Feature | Status |
| :--- | :---: |
| Root OpenAPI Object (`openapi`, `$self`, `jsonSchemaDialect`, `info`, `servers`, `webhooks`, `tags`, `externalDocs`) | ✅ |
| Info, Contact & License (`summary`, `identifier`, `termsOfService`) | ✅ |
| Server & ServerVariable (`url`, `description`, `name`, `enum`, `default`) | ✅ |
| Paths & PathItem (`$ref`, `summary`, `description`, `get`, `put`, `post`, `delete`, `options`, `head`, `patch`, `trace`, `query`, `additionalOperations`) | ✅ |
| Operation, Parameter, Header, RequestBody, MediaType (`itemSchema`, `itemEncoding`, `prefixEncoding`), Encoding | ✅ |
| Responses, Response, Example (`dataValue`, `serializedValue`, `externalValue`), Link, Callback | ✅ |
| Schema (JSON Schema 2020-12 keywords, `allOf`, `anyOf`, `oneOf`, `not`, `const`, `discriminator`, `xml`, `additionalProperties`) | ✅ |
| SecurityScheme (`apiKey`, `http`, `mutualTLS`, `oauth2` with `deviceAuthorization`, `openIdConnect`, `oauth2MetadataUrl`) & SecurityRequirement | ✅ |
| Tag (`parent`, `kind`, `summary`) & Reference Object (`$ref`, `summary`, `description`) | ✅ |
| Swagger 2.0 Automatic Up-Conversion (`swagger: "2.0"`, `host`, `basePath`, `Definitions`, `securityDefinitions`) | ✅ |

### Arazzo 1.1.0

| Feature | Status |
| :--- | :---: |
| Root Arazzo Object (`arazzo`, `$self`, `info`, `sourceDescriptions`, `workflows`, `components`) | ✅ |
| SourceDescription (`name`, `url`, `type: openapi \| arazzo`) | ✅ |
| Workflow (`workflowId`, `summary`, `description`, `inputs`, `dependsOn`, `steps`, `successActions`, `failureActions`, `outputs`, `parameters`) | ✅ |
| Step (`stepId`, `operationId`, `operationPath`, `workflowId`, `parameters`, `requestBody`, `successCriteria`, `onSuccess`, `onFailure`, `outputs`) | ✅ |
| Criterion (`condition`, `context`, `type: simple \| regex \| jsonpath \| xpath`, `version`) | ✅ |
| SuccessAction (`end`, `goto`) & FailureAction (`end`, `retry`, `goto`, `retryAfter`, `retryLimit`) | ✅ |
| Reusable Objects (`$components.parameters.*`, `$components.successActions.*`, `$components.failureActions.*`, `$components.inputs.*`) | ✅ |
| Runtime Expressions (`$url`, `$method`, `$statusCode`, `$request.*`, `$response.*`, `$inputs.*`, `$outputs.*`, `$steps.*`, `$workflows.*`, `$sourceDescriptions.*`, `$components.*`) | ✅ |

### Model Context Protocol (MCP) 1.0.0

| Feature | Status |
| :--- | :---: |
| Embedded SDK `McpAdapter` (`getTools()`, `callTool()`, `getResources()`, `mcpSse()`) | ✅ |
| MCP JSON-RPC 2.0 Schema POJOs (`Tool`, `Resource`, `Prompt`, `CallToolRequest`, `CallToolResult`, `InitializeRequest`, `InitializeResult`, `SamplingMessage`, etc.) | ✅ |
| Compiler MCP Server (`cdd-java mcp` / `cdd-java serve_json_rpc` over HTTP and STDIO) | ✅ |
| Live AST Resource Endpoints (`cdd://ast/openapi`, `cdd://ast/arazzo`) & Tool Execution (`cdd_generate`) | ✅ |
