# Developing cdd-java

## Requirements
* Java Development Kit (JDK) 11 or higher.
* `make` (or `make.bat` for Windows).

## Building
Use the Makefile:
```sh
make install_deps
make build
```

## Running Tests
Tests use an internal test runner over `src/test/java/`.
```sh
make test
```

We aim for 100% test coverage and 100% docstring coverage. Make sure to run the test suite and verify coverage before committing. Pre-commit hooks should enforce this.
