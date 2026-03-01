# Developing `cdd-java`

This guide explains how to set up your environment to develop, build, and test the `cdd-java` tool itself.

## Prerequisites

1.  **JDK 11 or higher:** The tool leverages `java.net.http.HttpClient` and `com.sun.net.httpserver.HttpServer`, requiring at least JDK 11.
2.  **Required Libraries:** Ensure the following dependency `.jar` files exist in the `/lib/` directory:
    *   `jackson-annotations-2.15.2.jar`
    *   `jackson-core-2.15.2.jar`
    *   `jackson-databind-2.15.2.jar`
    *   `javaparser-core-3.25.8.jar`

## Project Structure

The project code is divided between main execution and testing:
*   **`src/main/java/`**: Contains the core logic for the transpiler (OpenAPI models, CLI entrypoint, Emit/Parse classes).
*   **`src/test/java/`**: Contains the internal testing suite (`TestRunner.java`, `FullCoverageTest.java`).

## Compiling the Source

Because `cdd-java` is designed to be lightweight, you can compile it directly using `javac`, including the necessary libraries on the classpath.

From the repository root:
```bash
# Compile all source files into a target/classes directory
mkdir -p target/classes
javac -cp "lib/*" -d target/classes $(find src/main/java -name "*.java")
```

To run the CLI from the compiled classes:
```bash
java -cp "lib/*:target/classes" cli.Main --help
```
*(Note: the `cdd-java` root executable script handles this classpath setup automatically).*

## Running Tests

The project utilizes a custom, reflection-based testing suite to ensure 100% test coverage and validation of internal constraints without requiring JUnit.

To execute the test suite:
```bash
# Compile the test classes
javac -cp "lib/*:target/classes" -d target/classes $(find src/test/java -name "*.java")

# Run the TestRunner
java -cp "lib/*:target/classes" TestRunner
```

## Adding Features

1.  **OpenAPI Compliance:** If extending `from_openapi` or `to_openapi`, ensure your changes accurately reflect the OpenAPI 3.2.0 specification.
2.  **No New Dependencies:** Any new feature must be implemented using core Java (JDK 11+). Do not add new third-party libraries without exceptional architectural justification.
3.  **Documentation:** All new classes and public methods must be accompanied by comprehensive Javadoc.
4.  **Testing:** Any new class added must be validated by `TestRunner.java`.
