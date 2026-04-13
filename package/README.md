# @cdd/java-wasm

This is the official WebAssembly engine for `cdd-java`.
It provides a universal JavaScript wrapper over the GraalVM WASI binary, allowing you to generate SDKs, CLIs, Server Routes, and ORM entities directly in the browser or in Node.js without any Java runtime.

## Installation

```bash
npm install @cdd/java-wasm
```

## Usage

```javascript
import { CddJavaWasm } from '@cdd/java-wasm';
// The bundler can resolve the wasm file (e.g. Vite, Webpack)
import wasmUrl from '@cdd/java-wasm/wasm?url';

async function main() {
    // 1. Fetch the WASM binary
    const response = await fetch(wasmUrl);
    const wasmBuffer = await response.arrayBuffer();

    // 2. Initialize the engine
    const engine = new CddJavaWasm(wasmBuffer);

    // 3. Generate
    const specJson = JSON.stringify({
        openapi: "3.2.0",
        info: { title: "Example API", version: "1.0.0" },
        paths: {}
    });

    const result = await engine.generateSdk(specJson);
    
    // 4. Access output
    console.log("Stdout:", result.stdout);
    console.log("Files:", Object.keys(result.generatedFiles));
    
    // Example: result.generatedFiles["Sdk.java"] contains the generated code
}

main();
```

## Available Methods

* `generateSdk(specJsonStr)`
* `generateSdkCli(specJsonStr)`
* `generateServer(specJsonStr)`
* `generateOrm(specJsonStr)`
