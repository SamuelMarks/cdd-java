# WebAssembly (WASM) Support in cdd-java

| Target | Is WASM Possible? | Is WASM Implemented? |
|--------|-------------------|----------------------|
| cdd-java | Yes (via TeaVM/J2CL) | No |

WASM compilation is currently out-of-scope for the native cdd-java Makefile using emsdk, as Java requires JVM-to-WASM compilers like GraalVM, J2CL, or TeaVM.
