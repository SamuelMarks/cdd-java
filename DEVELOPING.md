# Developing
Run `make test` for running the test suite.
Run `make build_docs` to build Javadoc.\n## WASM Compilation\nTo build the standalone WASI binary locally, ensure you have set:\n- `GRAALVM_HOME` pointing to a GraalVM installation that supports the `wasm32-wasi` target.\n- `WASI_SDK_PATH` pointing to the extracted WASI SDK.\n\nRun `./build_wasm.sh` to compile.
