# WASM Support

| Feature | Supported | Implemented |
|---------|-----------|-------------|
| WASM Build | ✅ Possible | ✅ Yes |

WASM support for Java is currently implemented natively in this repository using GraalVM `native-image` targeting `wasm32-wasi`.

## Running the WASM

To run the WASM binary locally, you need a WASI-compliant runtime like `wasmtime` or `wasmer`.
You must explicitly grant directory access using the `--dir .` flag.

**Using Wasmtime:**
```bash
wasmtime --dir . target/wasm/cdd-java.wasm --help
wasmtime --dir . target/wasm/cdd-java.wasm from_openapi to_sdk -i spec.json -o out_sdk
```

**Using Wasmer:**
```bash
wasmer run --dir . target/wasm/cdd-java.wasm -- --help
wasmer run --dir . target/wasm/cdd-java.wasm -- from_openapi to_sdk -i spec.json -o out_sdk
```

**JSON-RPC:**
The tool uses a stdio-based JSON-RPC server implementation. TCP sockets are unavailable in WASI Preview 1.

```bash
echo '{"jsonrpc":"2.0","method":"version","id":1}' | wasmtime target/wasm/cdd-java.wasm serve_json_rpc
```
