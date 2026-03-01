# Compliance and Constraints

`cdd-java` strictly adheres to **Contract-Driven Development** principles. This document outlines the core compliance tenets and constraints the tool enforces both internally and in the code it generates.

## Contract-Driven Interoperability

The primary directive of `cdd-java` is **100% interoperability** between the documentation contract (`openapi.json`) and the implementing code.
* If a model exists in the API specification, it must exist exactly as described in the code.
* If code defines a public schema or route, it must be completely and accurately reflectable back into an OpenAPI specification.

## 100% Core Java Constraint

To minimize dependency bloat, reduce attack surfaces, and ensure long-term stability:
* **No Frameworks:** `cdd-java` explicitly avoids generating code dependent on Spring, Netty, or Retrofit.
* **Routing:** All API clients utilize the standard `java.net.http.HttpClient` introduced in JDK 11.
* **Server/Mocking:** All generated servers utilize the standard `com.sun.net.httpserver.HttpServer`.
* **Third-Party Libraries:** The only permissible dependencies are `jackson` (for JSON serialization) and `javaparser` (internally, for the CLI's parsing capability).

## 100% Documented Code

All internal classes of `cdd-java` and **every generated artifact** must include Javadoc `/** ... */` annotations.
* Properties in OpenAPI must translate to Javadoc properties on generated fields.
* Javadoc on parsed Java code must translate cleanly into `description` and `summary` fields within the `openapi.json`.

## 100% Tested

`cdd-java` mandates testability.
* **Internal:** Features a custom reflection-based coverage suite spanning all internal system configurations.
* **Emitted:** The `from_openapi` command inherently emits a `main` execution loop utilizing assertions to establish a complete testing suite for the generated clients and mock servers, guaranteeing that the generated code is immediately verifiable.
