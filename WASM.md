# WASM Support

| Feature | Supported | Implemented |
|---------|-----------|-------------|
| WASM Build | ✅ Possible | ✅ Yes |
| Browser/Node.js SDK | ✅ Possible | ✅ Yes |

WASM support for Java is currently implemented natively in this repository using GraalVM `native-image` targeting WasmGC via the `--tool:svm-wasm` backend.

## Local Compilation Requirements

To compile the WebAssembly module natively, you need **GraalVM 25 EA** (or newer). 

If you do not have GraalVM 25 installed locally or `GRAALVM_HOME` set to it, the `build_wasm.sh` script will automatically download the correct GraalVM 25 EA release for your operating system and architecture (macOS or Linux) and use it to compile the WASM binary. 

You no longer need Docker or any container runtimes to compile WASM on macOS. It compiles natively!

## Usage in JavaScript (Browser & Node.js)

Because the WasmGC target relies on complex bridging between JavaScript and WebAssembly for memory management and Garbage Collection, it requires executing through GraalVM's generated JavaScript wrapper `cdd-java.js`.

We publish a universal NPM wrapper that elegantly handles this execution in-memory.

### Installation

```bash
npm install @cdd/java-wasm
```

### Usage

```javascript
import { CddJavaBrowser } from '@cdd/java-wasm'; // Or 'cdd-java-cli' depending on your package.json

// Depending on your environment, you must ensure the GraalVM JS wrapper is loaded.
// In the browser, you can load it via a <script> tag:
// <script src="assets/wasm/cdd-java.js"></script>

// Instantiate the engine, pointing it to the WASM file
const engine = new CddJavaBrowser('./cdd-java.wasm');

const specJson = JSON.stringify({
    openapi: "3.2.0",
    info: { title: "Example", version: "1.0" },
    paths: {}
});

// Run generation in-memory
const result = await engine.generateSdk(specJson);

// Output
console.log(result.output);
console.log(result.files["Sdk.java"]); 
```

### Technical Notes on Web Workers

If you are running the engine inside a Web Worker, you must manually ensure that the GraalVM object is mapped correctly after loading `cdd-java.js`, as it relies on being globally attached:

```javascript
// Inside your worker:
importScripts('/path/to/cdd-java.js');
if (self.GraalVM) {
    globalThis.GraalVM = self.GraalVM;
}
```
