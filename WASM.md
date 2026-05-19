# WASM Support

| Feature | Supported | Implemented |
|---------|-----------|-------------|
| WASM Build | ✅ Possible | ✅ Yes |
| Browser/Node.js SDK | ✅ Possible | ✅ Yes |

WASM support for Java is currently implemented natively in this repository using GraalVM `native-image` targeting `wasm32-wasi`.

## Local Compilation Requirements & Technical Limitations

To produce a "pure WASI" binary, the `build_wasm.sh` script explicitly utilizes **GraalVM CE 22.3**. 

### Why GraalVM 22.3 and not newer versions?
While newer versions (GraalVM 24+ / 25 EA) do support WASM compilation natively on macOS via the `--tool:svm-wasm` flag, **Oracle completely removed WASI standard support** in these versions. The newer backend targets the WasmGC proposal and relies entirely on a generated JavaScript wrapper. Crucially, this wrapper does not emulate a virtual filesystem in the browser. Since `cdd-java` fundamentally requires file I/O (reading `spec.json` and writing to `out/`), using the newer GraalVM versions would cause the WebAssembly module to crash in browser environments like `cdd-web-ui`.

### Why Docker on macOS?
Because `cdd-java` requires a pure WASI binary to interface with virtual filesystems (via `@bjorn3/browser_wasi_shim`), GraalVM 22.3 is mandatory. However, **Oracle never compiled the WASI/LLVM backend into the macOS versions of GraalVM 22.3**. The `-H:WasiSdkPath` flag required for WASI compilation was an experimental feature shipped exclusively in the Linux binaries.

Therefore, if you are developing locally on macOS (especially Apple Silicon / ARM64), the `build_wasm.sh` script is designed to transparently fall back to Docker. It will utilize an `ubuntu:22.04` AMD64 container to natively cross-compile the WASI binary. **You must have a Docker daemon running** (e.g. Docker Desktop, OrbStack, Colima) to build the WASM target locally on a Mac.

## Usage in JavaScript (Browser & Node.js)

To support broader tooling like `cdd-ctl` and `cdd-web-ui`, the WASI binary is published as a universal NPM package using a virtual filesystem shim.

### Installation

```bash
npm install @cdd/java-wasm
```

### Usage

```javascript
import { CddJavaWasm } from '@cdd/java-wasm';

// Depending on your environment, you load the .wasm file into an ArrayBuffer:
// Browser:  const buffer = await (await fetch('https://github.com/SamuelMarks/cdd-java/releases/download/latest/cdd-java.wasm')).arrayBuffer();
// Node.js:  const buffer = await require('fs/promises').readFile('path/to/cdd-java.wasm');

const engine = new CddJavaWasm(buffer);

const specJson = JSON.stringify({
    openapi: "3.2.0",
    info: { title: "Example", version: "1.0" },
    paths: {}
});

// Run generation in-memory
const result = await engine.generateSdk(specJson);

// Output
console.log(result.stdout);
console.log(result.generatedFiles["Sdk.java"]); 
```

## Running the WASM locally via CLI

To run the standalone WASM binary locally, you need a WASI-compliant runtime like `wasmtime` or `wasmer`.
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
