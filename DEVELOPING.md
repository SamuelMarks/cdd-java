# Developing `cdd-java`

## Prerequisites

- **Java JDK**: Java 11 or newer (JDK 11–23 recommended when running JaCoCo coverage instrumentation).
- **Apache Maven**: `3.8+`.
- **Python 3**: Used for helper verification scripts under `scripts/`.

---

## Building & Running Tests

```sh
# Run the full test suite and package the shaded JAR (target/cdd-java-0.0.4.jar)
make test

# Run tests with 100% JaCoCo instruction, line, branch, method, and class coverage enforcement
mvn test jacoco:report jacoco:check

# Run strict Javadoc doclint verification via Maven
mvn javadoc:javadoc

# Verify code formatting (Eclipse JDT formatter via Spotless)
mvn spotless:check

# Apply formatting automatically
mvn spotless:apply
```

---

## Documentation Coverage & Javadoc

`cdd-java` enforces 100% Javadoc coverage across `src/main/java/`:

```sh
# Check for any missing Javadoc comments
python3 scripts/check_missing_docs.py

# Build HTML Javadoc into target/docs (symlinked at docs/html)
make docs

# Build HTML Javadoc into a custom directory
make build_docs docs
```

---

## WASM Compilation

To build the standalone WASI binary locally, ensure you have set:
- `GRAALVM_HOME` pointing to a GraalVM installation that supports the `wasm32-wasi` target (`svm-wasm`).
- `WASI_SDK_PATH` pointing to the extracted WASI SDK.

```sh
# Compile cdd-java to WebAssembly and build the JS/TS wrapper in package/
make build_wasm
```
