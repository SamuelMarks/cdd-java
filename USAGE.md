# Usage

`cdd-java` serves as a bidirectional compiler for OpenAPI to Java.

### Generate an OpenAPI Spec from Code
Scan your codebase to generate `spec.json`.
```sh
cdd-java to_openapi -f ./src/main/java/ -o spec.json
```

### Generate Java SDK from OpenAPI
Generate an SDK to `my_sdk_dir/`. By default, generates a `pom.xml` and GitHub Actions CI file as well.
```sh
cdd-java from_openapi to_sdk -i spec.json -o my_sdk_dir/
```

### Generate Server Routes
Generate network routing code to wire up the API endpoints.
```sh
cdd-java from_openapi to_server -i spec.json -o ./src/main/java/routes/
```

### Serve via JSON-RPC
Starts an HTTP JSON-RPC server on port 8080.
```sh
cdd-java server_json_rpc --port 8080 --listen 0.0.0.0
```

### Emit Docs
Emit a JSON document representing code models for documentation tools.
```sh
cdd-java to_docs_json --no-imports --no-wrapping -i spec.json -o docs.json
```
