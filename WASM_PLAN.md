# Comprehensive WASM Migration Plan for cdd-java

This document outlines the exhaustive, step-by-step strategy for refactoring `cdd-java` to compile into a self-contained WebAssembly (WASI) binary using GraalVM.

## Phase 1: Environment Diagnostics & GraalVM Toolchain Setup

### System Prerequisites Validation
- [x] Verify host OS compatibility for GraalVM `native-image` WASM cross-compilation.
- [x] Ensure Python 3 is installed (used for auxiliary scripts in the repo).
- [x] Ensure `curl` or `wget` is available for downloading external testing binaries.
- [x] Ensure `tar` and `gzip` are available for extracting toolchains.
- [x] Ensure basic C compilation tools are available (Xcode Command Line Tools on macOS).

### Local GraalVM Verification
- [x] Verify GraalVM directory exists: `graalvm-community-openjdk-22.0.1+8.1`.
- [x] Validate `java` executable path: `./graalvm-community-openjdk-22.0.1+8.1/Contents/Home/bin/java`.
- [x] Validate `javac` executable path: `./graalvm-community-openjdk-22.0.1+8.1/Contents/Home/bin/javac`.
- [x] Validate `native-image` executable path: `./graalvm-community-openjdk-22.0.1+8.1/Contents/Home/bin/native-image`.
- [x] Run `./graalvm-community-openjdk-22.0.1+8.1/Contents/Home/bin/native-image --version` to confirm version is 22.0.1.
- [x] Export `GRAALVM_HOME` pointing to absolute path of `./graalvm-community-openjdk-22.0.1+8.1/Contents/Home`.
- [x] Prepend `$GRAALVM_HOME/bin` to the local `PATH` variable in the development shell.

### Local WASI SDK Verification
- [x] Verify `wasi-sdk` directory exists: `wasi-sdk-24.0-arm64-macos`.
- [x] Validate `clang` executable exists in `wasi-sdk-24.0-arm64-macos/bin`.
- [x] Validate `wasm-ld` executable exists in `wasi-sdk-24.0-arm64-macos/bin`.
- [x] Validate `wasi-sysroot` exists in `wasi-sdk-24.0-arm64-macos/share/wasi-sysroot`.
- [x] Export `WASI_SDK_PATH` pointing to absolute path of `./wasi-sdk-24.0-arm64-macos`.

### Build Script Scaffolding (`build_wasm.py`)
- [x] Create a new executable script `build_wasm.py` in the project root.
- [x] Add `#!/bin/bash` shebang to `build_wasm.py`.
- [x] Add `set -e` to fail the script on any errors.
- [x] Add validation to assert `GRAALVM_HOME` is set.
- [x] Add validation to assert `WASI_SDK_PATH` is set.
- [x] Add `echo "Starting GraalVM WASM compilation..."` for debug visibility.
- [x] Scaffold the `native-image` invocation block within the script.
- [x] Add flag `--target=wasm32-wasi` to the `native-image` command.
- [x] Add flag `-H:WasiSdkPath=$WASI_SDK_PATH` to explicitly link the SDK.
- [x] Add flag `--no-fallback` to ensure pure standalone binary compilation.
- [x] Add flag `-cp target/cdd-java-0.0.2-jar-with-dependencies.jar` to define the classpath.
- [x] Provide the entrypoint class `cli.Main` to the `native-image` command.
- [x] Define output file parameter `-o target/wasm/cdd-java`.
- [x] Make `build_wasm.py` executable via `chmod +x build_wasm.py`.

## Phase 2: Maven pom.xml & Build Lifecycle Overhaul

### Cleanup Legacy TeaVM Configuration
- [x] Open the root `pom.xml`.
- [x] Locate `<profile id="wasm">`.
- [x] Remove the inner `<plugin>` block for `maven-compiler-plugin` containing the malformed `sourceDirectory` tag.
- [x] Remove the inner `<plugin>` block for `teavm-maven-plugin`.
- [x] Remove `src/wasm/java` source directory reference from the POM entirely.
- [x] Delete the `src/wasm/java` directory from the filesystem to prevent confusion.
- [x] Delete the `wasm_stub` directory from the filesystem entirely.

### Global Compiler Upgrades
- [x] Locate `maven.compiler.source` property and change `11` to `22`.
- [x] Locate `maven.compiler.target` property and change `11` to `22`.
- [x] Locate `maven.compiler.release` property and change `11` to `22`.
- [x] Verify `maven-assembly-plugin` configuration explicitly targets `cli.Main` in the manifest.

### GraalVM Native Maven Plugin Integration
- [x] Add `<plugin>` definition for `org.graalvm.buildtools:native-maven-plugin`.
- [x] Set plugin version to `0.10.1` (or compatible version for GraalVM 22).
- [x] Add `<extensions>true</extensions>` to the native plugin configuration.
- [x] Bind the native plugin to the `package` phase.
- [x] Add `<buildArg>--target=wasm32-wasi</buildArg>` to the plugin configuration.
- [x] Add `<buildArg>-H:WasiSdkPath=${env.WASI_SDK_PATH}</buildArg>` to the plugin configuration.
- [x] Ensure `<metadataRepository><enabled>true</enabled></metadataRepository>` is set.
- [x] Ensure `<imageName>cdd-java</imageName>` is configured.
- [x] Set up `<fallback>false</fallback>` inside plugin configuration.
- [x] Verify standard `mvn clean package` successfully builds the fat JAR without triggering WASM compilation implicitly.

## Phase 3: Jackson JSON Removal & Reflection Elimination

### Deprecating Jackson Dependencies
- [x] Open `pom.xml`.
- [x] Remove dependency block for `com.fasterxml.jackson.core:jackson-core`.
- [x] Remove dependency block for `com.fasterxml.jackson.core:jackson-databind`.
- [x] Remove dependency block for `com.fasterxml.jackson.core:jackson-annotations`.

### Adopting AOT-Friendly JSON Processing (e.g., org.json)
- [x] Add dependency block for `org.json:json` (latest version, e.g., 20240303).
- [x] Verify `org.json` builds locally without reflection warnings in GraalVM.
- [x] Remove `import com.fasterxml.jackson.databind.ObjectMapper;` from `cli.Main.java`.
- [x] Remove `import com.fasterxml.jackson.databind.JsonNode;` from `cli.Main.java`.
- [x] Remove Jackson imports from `openapi/Parse.java`.
- [x] Remove Jackson imports from `openapi/Emit.java`.

### Refactoring `openapi.Parse.java`
- [x] Change `Parse.fromFile` to read the file into a pure `String`.
- [x] Parse the string using `new org.json.JSONObject(content)`.
- [x] Map `JSONObject` root to the `openapi.OpenAPI` Java model manually.
- [x] Map `openapi` field (String).
- [x] Map `info` object -> `openapi.Info`.
- [x] Map `info.title` (String).
- [x] Map `info.version` (String).
- [x] Map `info.description` (String, optional).
- [x] Map `paths` object -> `openapi.Paths`.
- [x] Iterate over `paths` keys, mapping each to `openapi.PathItem`.
- [x] Map `PathItem.get` -> `openapi.Operation`.
- [x] Map `PathItem.post` -> `openapi.Operation`.
- [x] Map `PathItem.put` -> `openapi.Operation`.
- [x] Map `PathItem.delete` -> `openapi.Operation`.
- [x] Map `Operation.operationId` (String).
- [x] Map `Operation.summary` (String).
- [x] Map `Operation.parameters` -> `List<openapi.Parameter>`.
- [x] Map `Operation.requestBody` -> `openapi.RequestBody`.
- [x] Map `Operation.responses` -> `Map<String, openapi.Response>`.
- [x] Map `components` object -> `openapi.Components`.
- [x] Map `components.schemas` -> `Map<String, openapi.Schema>`.
- [x] Implement robust type checking (e.g., `jsonObject.has("x")` before calling `getString("x")`).
- [x] Add explicit error handling for malformed JSON without relying on Jackson's `JsonMappingException`.

### Refactoring `openapi.Emit.java`
- [x] Change `Emit.toString` to return a `org.json.JSONObject`.
- [x] Instantiate an empty `JSONObject` for the root document.
- [x] Put `openapi` version string into the root object.
- [x] Serialize `info` object to a nested `JSONObject`.
- [x] Serialize `paths` to a nested `JSONObject`.
- [x] Serialize all individual `PathItem` objects into the paths object.
- [x] Serialize `components` and `schemas` to a nested `JSONObject`.
- [x] Return the final string representation using `rootObj.toString(2)` for pretty-printing.

## Phase 4: `java.nio` API Eradication

### File System Isolation Analysis
- [x] Search codebase for `import java.nio.file.Files;`.
- [x] Search codebase for `import java.nio.file.Path;`.
- [x] Search codebase for `import java.nio.file.Paths;`.
- [x] Search codebase for `import java.nio.file.StandardOpenOption;`.

### Replacing `java.nio` in `cli.Main.java`
- [x] Replace `Files.write(..., StandardOpenOption...)` in `to_sdk_cli` handler with `java.io.FileOutputStream` / `java.io.FileWriter`.
- [x] Replace `Files.write` in `to_sdk` handler with IO equivalents.
- [x] Replace `Files.write` in `to_server` handler with IO equivalents.
- [x] Replace `Files.write` in `to_orm` handler with IO equivalents.
- [x] Replace `Files.write` in `to_openapi` handler with IO equivalents.
- [x] Replace `Files.write` in `to_docs_json` handler with IO equivalents.
- [x] Replace `Files.write` in `sync` handler with IO equivalents.
- [x] Replace `Files.write` inside `generateScaffolding` method.
- [x] Replace `Files.write` inside `generateGithubActions` method.
- [x] Replace `Files.readAllBytes` in `extractOpenAPI` with a custom stream reader loop or `Scanner`.
- [x] Replace `Files.readAllBytes` in `sync` method with an IO equivalent.
- [x] Refactor `outDir.mkdirs()` to handle edge cases if WASI restricts recursive directory creation.

### Abstracting Directory Traversals
- [x] Review `findJavaFiles` recursive method in `cli.Main`.
- [x] Ensure `dir.listFiles()` properly falls back or errors safely if WASI encounters permission issues.
- [x] Hardcode paths to use `/` universally, avoiding `File.separator` which may resolve strangely in WASI depending on the host compile environment.

## Phase 5: Sockets Elimination & Stdio RPC Implementation

### Removing TCP Server
- [x] Locate `startJsonRpcServer` in `cli.Main`.
- [x] Remove `import com.sun.net.httpserver.HttpServer;`.
- [x] Remove `import com.sun.net.httpserver.HttpHandler;`.
- [x] Remove `import com.sun.net.httpserver.HttpExchange;`.
- [x] Remove `import java.net.InetSocketAddress;`.
- [x] Delete the implementation inside `startJsonRpcServer` that initializes `HttpServer.create(...)`.

### Creating Stdio JSON-RPC Loop
- [x] Create a new method: `private static void startStdioJsonRpcServer() throws Exception`.
- [x] Initialize a `BufferedReader` reading from `System.in`.
- [x] Create a continuous `while ((line = reader.readLine()) != null)` loop.
- [x] Parse each line as a JSON-RPC request using `org.json.JSONObject`.
- [x] Validate presence of `"jsonrpc": "2.0"` field.
- [x] Extract `"method"` and `"id"` fields.
- [x] Switch on the `"method"` value (e.g., `case "version":`).
- [x] For `version` method, formulate success response: `{"jsonrpc":"2.0","result":"0.0.2","id":<id>}`.
- [x] For unknown methods, formulate error response: `{"jsonrpc":"2.0","error":{"code":-32601,"message":"Method not found"},"id":<id>}`.
- [x] Print the response payload strictly to `System.out`.
- [x] Add `System.out.flush()` after printing to prevent buffering blocks in IPC.
- [x] Add error handling: wrap the parsing in a `try/catch`.
- [x] On parse error, output `{"jsonrpc":"2.0","error":{"code":-32700,"message":"Parse error"},"id":null}`.
- [x] Update `cli.Main.main` to route `serve_json_rpc` commands to `startStdioJsonRpcServer`.
- [x] Prevent any other `System.out.println()` statements from firing when in RPC mode to avoid corrupting JSON output.

### CLI Flag Updates for WASI
- [x] Add a `--wasi` flag parsing in `cli.Main` to toggle specific runtime behaviors if needed later.
- [x] Update the `printHelp` command to reflect changes (remove `--listen` and `--port` if standardizing purely on stdio).

## Phase 6: GraalVM Tracing & Configuration Generation

### Preparing the Tracing Environment
- [x] Rebuild the standard JVM fat JAR: `mvn clean package`.
- [x] Verify `target/cdd-java-0.0.2-jar-with-dependencies.jar` exists.
- [x] Create an output directory: `mkdir -p META-INF/native-image/org.cdd/cdd-java`.
- [x] Clear existing config files in `META-INF/native-image/` to prevent cross-contamination.
- [x] Create a mock `spec.json` file for the agent to process during tracing.
- [x] Create a mock `java_source_dir/Mock.java` file for the agent to parse during tracing.

### Executing Tracing Runs
- [x] Run Trace 1 (Help): `java -agentlib:native-image-agent=config-merge-dir=META-INF/native-image -jar target/cdd-java-0.0.2-jar-with-dependencies.jar --help`
- [x] Run Trace 2 (Version): `java -agentlib:native-image-agent=config-merge-dir=META-INF/native-image -jar target/cdd-java-0.0.2-jar-with-dependencies.jar --version`
- [x] Run Trace 3 (to_openapi): `java -agentlib:native-image-agent=config-merge-dir=META-INF/native-image -jar target/cdd-java-0.0.2-jar-with-dependencies.jar to_openapi -i java_source_dir -o traced_out.json`
- [x] Run Trace 4 (to_sdk_cli): `java -agentlib:native-image-agent=config-merge-dir=META-INF/native-image -jar target/cdd-java-0.0.2-jar-with-dependencies.jar from_openapi to_sdk_cli -i spec.json -o out_traced`
- [x] Run Trace 5 (to_sdk): `java -agentlib:native-image-agent=config-merge-dir=META-INF/native-image -jar target/cdd-java-0.0.2-jar-with-dependencies.jar from_openapi to_sdk -i spec.json -o out_traced`
- [x] Run Trace 6 (to_server): `java -agentlib:native-image-agent=config-merge-dir=META-INF/native-image -jar target/cdd-java-0.0.2-jar-with-dependencies.jar from_openapi to_server -i spec.json -o out_traced`
- [x] Run Trace 7 (to_orm): `java -agentlib:native-image-agent=config-merge-dir=META-INF/native-image -jar target/cdd-java-0.0.2-jar-with-dependencies.jar from_openapi to_orm -i spec.json -o out_traced`
- [x] Run Trace 8 (to_docs_json): `java -agentlib:native-image-agent=config-merge-dir=META-INF/native-image -jar target/cdd-java-0.0.2-jar-with-dependencies.jar to_docs_json -i spec.json -o docs_traced.json`
- [x] Run Trace 9 (RPC): `echo '{"jsonrpc":"2.0","method":"version","id":1}' | java -agentlib:native-image-agent=config-merge-dir=META-INF/native-image -jar target/cdd-java-0.0.2-jar-with-dependencies.jar serve_json_rpc`

### Validating Generated Configurations
- [x] Inspect `META-INF/native-image/reflect-config.json`.
- [x] Ensure `com.github.javaparser.ast.*` nodes appear in the reflect config (e.g., `MethodDeclaration`, `ClassOrInterfaceDeclaration`).
- [x] Inspect `META-INF/native-image/resource-config.json`.
- [x] Ensure any `.properties` or `.xml` loaded internally by `javaparser-core` are registered.
- [x] Inspect `META-INF/native-image/proxy-config.json` for dynamically generated proxy arrays.
- [x] Inspect `META-INF/native-image/jni-config.json` (should be mostly empty or negligible for this CLI).

## Phase 7: GraalVM WASM Build Execution & Optimization

### First Compilation Pass
- [x] Execute `./build_wasm.py`.
- [x] Monitor log for "Unsupported API" errors (indicates missed `java.net` or `java.nio` references).
- [x] Monitor log for Threading API errors (e.g., `java.lang.Thread.start()` is restricted in WASI).
- [x] Provide stub implementations for any missed unsupported JDK classes using GraalVM substitutions (`@Substitute`) if absolutely necessary.
- [x] Ensure successful termination resulting in a `.wasm` file in `target/wasm/cdd-java.wasm`.

### Advanced Build Tuning
- [x] Add `--initialize-at-build-time=com.github.javaparser` to `build_wasm.py` to shift AST initialization out of the runtime.
- [x] Add `--initialize-at-build-time=org.json` to the build script.
- [x] Add `-O3` flag for aggressive WASM size and speed optimizations.
- [x] Add `-g0` to strip DWARF debugging symbols to reduce binary size.
- [x] Re-run `./build_wasm.py` and compare binary size against the first pass.

## Phase 8: Comprehensive Local WASM Testing (Wasmtime / Wasmer)

### Testing Runtime Installations
- [x] Install `wasmtime` locally (e.g., `curl https://wasmtime.dev/install.sh -sSf | bash`).
- [x] Verify `wasmtime --version` outputs successfully.
- [x] Install `wasmer` locally (e.g., `curl https://get.wasmer.io -sSfL | sh`).
- [x] Verify `wasmer --version` outputs successfully.

### Command Execution Parity Tests (Wasmtime)
- [x] Run `wasmtime target/wasm/cdd-java.wasm --help`. Verify correct output.
- [x] Run `wasmtime target/wasm/cdd-java.wasm --version`. Verify `0.0.2` output.
- [x] Create a `test_dir/spec.json`.
- [x] Run `wasmtime --dir . target/wasm/cdd-java.wasm from_openapi to_sdk -i test_dir/spec.json -o test_dir/out_sdk`.
- [x] Verify `test_dir/out_sdk/Sdk.java` is generated correctly.
- [x] Run `wasmtime --dir . target/wasm/cdd-java.wasm from_openapi to_server -i test_dir/spec.json -o test_dir/out_server`.
- [x] Verify `test_dir/out_server/ServerRoutes.java` is generated correctly.
- [x] Run `wasmtime --dir . target/wasm/cdd-java.wasm to_openapi -i src/main/java/cli -o test_dir/extracted_spec.json`.
- [x] Verify `test_dir/extracted_spec.json` contains valid OpenAPI syntax.
- [x] Test JSON-RPC via Pipe: `echo '{"jsonrpc":"2.0","method":"version","id":99}' | wasmtime target/wasm/cdd-java.wasm serve_json_rpc`.
- [x] Verify standard output is EXACTLY `{"jsonrpc":"2.0","result":"0.0.2","id":99}` with no extra log lines.

### Command Execution Parity Tests (Wasmer)
- [x] Run `wasmer run target/wasm/cdd-java.wasm -- --help`. Verify output.
- [x] Run `wasmer run --dir . target/wasm/cdd-java.wasm -- from_openapi to_orm -i test_dir/spec.json -o test_dir/out_orm`.
- [x] Verify `test_dir/out_orm/OrmEntities.java` is generated.
- [x] Test JSON-RPC via Pipe: `echo '{"jsonrpc":"2.0","method":"unknown","id":42}' | wasmer run target/wasm/cdd-java.wasm -- serve_json_rpc`.
- [x] Verify standard output is EXACTLY `{"jsonrpc":"2.0","error":{"code":-32601,"message":"Method not found"},"id":42}`.

### Edge Case & Boundary Testing
- [x] Test missing input file: `wasmtime --dir . target/wasm/cdd-java.wasm from_openapi to_sdk -i missing.json`. Expect graceful exit code 1.
- [x] Test missing `--dir` flag: `wasmtime target/wasm/cdd-java.wasm from_openapi to_sdk -i spec.json`. Ensure WASI virtual filesystem boundaries trigger clear errors instead of hard crashes.
- [x] Test malformed JSON-RPC input via stdin. Ensure it returns parse error `-32700`.

## Phase 9: Remote CI Testing via GitHub Actions

### Workflow Configuration (`.github/workflows/ci.yml`)
- [x] Open `.github/workflows/ci.yml`.
- [x] Ensure `on: [push, pull_request]` is set.
- [x] Define a matrix strategy for operating systems: `ubuntu-latest`, `macos-latest`.
- [x] Add a step to cache `~/.m2/repository` to speed up Maven dependencies.
- [x] Setup GraalVM step using `graalvm/setup-graalvm@v1`.
- [x] Configure `setup-graalvm` action for version `22.0.1` and `java-version: 22`.
- [x] Add a step to download and extract `wasi-sdk` dynamically based on runner OS (using `wget` and `tar`).
- [x] Export `WASI_SDK_PATH` to the extracted directory path in the GITHUB_ENV.
- [x] Add a step to run `mvn clean package` to build the standard JAR.
- [x] Add a step to execute `build_wasm.py` to generate the `.wasm` file.
- [x] Fail the CI run if the `.wasm` file does not exist.

### Remote WASM Integration Tests
- [x] Add a step to install Wasmtime (using curl install script) in the CI pipeline.
- [x] Add step: `wasmtime target/wasm/cdd-java.wasm --help`.
- [x] Add step: `echo '{"jsonrpc":"2.0","method":"version","id":1}' | wasmtime target/wasm/cdd-java.wasm serve_json_rpc > rpc_out.txt`.
- [x] Add step to assert the contents of `rpc_out.txt` strictly equal the expected JSON output.
- [x] Add step: `wasmtime --dir . target/wasm/cdd-java.wasm from_openapi to_sdk_cli -i mock_spec.json -o out_ci`.
- [x] Add step to assert `out_ci/SdkCli.java` exists in the CI environment.
- [x] Upload the `target/wasm/cdd-java.wasm` as a build artifact named `cdd-java-wasm-binary` using `actions/upload-artifact@v4`.

## Phase 10: Remote GitHub Release Pipeline

### Workflow Configuration (`.github/workflows/release-wasm.yml`)
- [x] Open `.github/workflows/release-wasm.yml`.
- [x] Update `on.push.tags` to trigger on `v*` and specific semver patterns.
- [x] Add `permissions: contents: write` to allow the action to create releases.
- [x] Add GraalVM setup step (`graalvm/setup-graalvm@v1`, Java 22).
- [x] Add WASI-SDK download and extraction step.
- [x] Set `WASI_SDK_PATH` environment variable.
- [x] Remove legacy `cd wasm_stub` logic.

### Release Artifact Generation
- [x] Execute `build_wasm.py` script to generate the production optimized `.wasm` artifact.
- [x] Validate `target/wasm/cdd-java.wasm` exists.
- [x] Rename artifact to include architecture context if desired, or keep as `cdd-java.wasm`.
- [x] Generate SHA-256 checksum for the `.wasm` file (e.g., `sha256sum target/wasm/cdd-java.wasm > cdd-java.wasm.sha256`).

### GitHub Release Creation
- [x] Utilize `softprops/action-gh-release@v2`.
- [x] Define `files:` payload to include `target/wasm/cdd-java.wasm` and `cdd-java.wasm.sha256`.
- [x] Define `body:` payload to include release notes, specifying it is a standalone WASI binary compiled via GraalVM.
- [x] Set `draft: false` and `prerelease: false`.
- [x] Test the release workflow by pushing a dummy tag to a sandbox branch (e.g., `v0.0.2-rc1`).
- [x] Verify the Release appears on GitHub with both assets downloadable.

## Phase 11: Cleanup, Documentation, & Git Hygiene

### Documentation Updates
- [x] Open `WASM.md`.
- [x] Change the status from "❌ No" to "✅ Yes".
- [x] Update the description to indicate GraalVM `native-image` targeting `wasm32-wasi` is now used.
- [x] Add a "Running the WASM" section showing example `wasmtime` and `wasmer` commands.
- [x] Explicitly document the requirement to use the `--dir .` flag with WASM runtimes to map file access.
- [x] Document the stdio JSON-RPC implementation explicitly, noting that TCP sockets are unavailable in WASI Preview 1.
- [x] Open `README.md`.
- [x] Add a small badge or section highlighting WASI compatibility.
- [x] Open `DEVELOPING.md` (or equivalent).
- [x] Document the `GRAALVM_HOME` and `WASI_SDK_PATH` requirements for compiling the WASM build locally.

### Removing Legacy Cruft
- [x] Delete `test_java_wasi.sh` from the repository.
- [x] Delete `test_java_wasi2.sh` from the repository.
- [x] Ensure `wasm_stub` directory is entirely deleted and untracked.
- [x] Clean up `.gitignore` to explicitly ignore `wasi-sdk-*` tarballs and extracted folders.
- [x] Clean up `.gitignore` to explicitly ignore `jdk-17-*` and `graalvm-*` downloads.
- [x] Clean up `.gitignore` to ignore the `target/wasm` output directory natively.

### Final Verification Commit
- [x] Run `git status` to ensure all necessary source files, build scripts, and workflow files are staged.
- [x] Run `mvn clean test` to ensure standard JVM execution wasn't broken by JSON or I/O refactoring.
- [x] Verify there are zero `import com.fasterxml.jackson` statements remaining via global search.
- [x] Verify there are zero `import java.nio` statements remaining via global search.
- [x] Verify there are zero `import java.net` statements remaining via global search.
- [x] Commit with message: "feat(wasm): refactor to full GraalVM WASI standalone build".
