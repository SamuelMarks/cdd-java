# @cdd/java-wasm (or cdd-java-cli)

This is the official WebAssembly engine for `cdd-java`.
It provides a universal JavaScript wrapper over the GraalVM 25 WasmGC binary, allowing you to generate SDKs, CLIs, Server Routes, and ORM entities directly in the browser or in Node.js without any Java runtime.

## Installation

```bash
npm install @cdd/java-wasm
```

## Usage

```javascript
import { CddJavaBrowser } from '@cdd/java-wasm';

// Depending on your environment, you must ensure the GraalVM JS wrapper is loaded.
// In the browser, you can load it via a <script> tag:
// <script src="assets/wasm/cdd-java.js"></script>

async function main() {
    // 1. Initialize the engine, pointing it to the WASM file location
    const engine = new CddJavaBrowser('./cdd-java.wasm');

    // 2. Generate
    const specJson = JSON.stringify({
        openapi: "3.2.0",
        info: { title: "Example API", version: "1.0.0" },
        paths: {}
    });

    const result = await engine.generateSdk(specJson);
    
    // 3. Access output
    console.log("Output Logs:", result.output);
    console.log("Files:", Object.keys(result.files));
    
    // Example: result.files["Sdk.java"] contains the generated code
}

main();
```

## Available Methods

* `generateSdk(specJsonStr, noGithubActions, noInstallablePackage)`
* `generateSdkCli(specJsonStr, noGithubActions, noInstallablePackage)`
* `generateServer(specJsonStr)`
* `generateOrm(specJsonStr)`
* `generateDocsJson(specJsonStr)`
