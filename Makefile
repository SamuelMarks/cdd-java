.PHONY: install_base install_deps build_docs build test run help all default

# Extract arguments for build_docs
ifeq (build_docs,$(firstword $(MAKECMDGOALS)))
  DOCS_ARGS := $(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))
  ifneq ($(DOCS_ARGS),)
    DOCS_DIR := $(word 1,$(DOCS_ARGS))
    $(eval $(DOCS_DIR):;@:)
  else
    DOCS_DIR := docs
  endif
else
  DOCS_DIR := docs
endif

# Extract arguments for build
ifeq (build,$(firstword $(MAKECMDGOALS)))
  BUILD_ARGS := $(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))
  ifneq ($(BUILD_ARGS),)
    BIN_DIR := $(word 1,$(BUILD_ARGS))
    $(eval $(BIN_DIR):;@:)
  else
    BIN_DIR := bin
  endif
else
  BIN_DIR := bin
endif

# Extract arguments for run
ifeq (run,$(firstword $(MAKECMDGOALS)))
  RUN_ARGS := $(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))
  $(eval $(RUN_ARGS):;@:)
endif

default: help

all: help

install_base:
	@echo "Installing Java runtime..."
	@if [ "$$(uname)" = "Darwin" ]; then \
		brew install openjdk; \
	elif [ -f /etc/debian_version ]; then \
		sudo apt-get update && sudo apt-get install -y default-jdk; \
	elif [ -f /etc/redhat-release ]; then \
		sudo yum install -y java-11-openjdk-devel; \
	elif [ -f /etc/freebsd-update.conf ]; then \
		sudo pkg install -y openjdk11; \
	else \
		echo "Please install a Java JDK manually."; \
	fi

install_deps:
	@echo "Dependencies already in lib/"

build_docs:
	@echo "Building API docs to $(DOCS_DIR)..."
	@mkdir -p "$(DOCS_DIR)"
	@find src/main/java -name "*.java" > doc_sources.txt
	javadoc -d "$(DOCS_DIR)" -cp "lib/*:src/main/java" @doc_sources.txt
	@rm -f doc_sources.txt

build:
	@echo "Building CLI to $(BIN_DIR)..."
	@mkdir -p "$(BIN_DIR)"
	@find src/main/java -name "*.java" > sources.txt
	javac -d "$(BIN_DIR)" -cp "lib/*:src/main/java" @sources.txt
	@rm -f sources.txt

test:
	@echo "Running tests..."
	@find src/main/java src/test/java -name "*.java" > sources.txt
	javac -cp "lib/*:src/main/java:src/test/java" @sources.txt
	java -cp "lib/*:src/main/java:src/test/java" TestRunner
	@rm -f sources.txt

run:
	@if [ ! -f "$(BIN_DIR)/cli/Main.class" ]; then \
		$(MAKE) build BIN_DIR="$(BIN_DIR)"; \
	fi
	java -cp "lib/*:$(BIN_DIR)" cli.Main $(RUN_ARGS)

help:
	@echo "Available targets:"
	@echo "  install_base : install language runtime (Java JDK)"
	@echo "  install_deps : install local dependencies"
	@echo "  build_docs   : build the API docs (e.g. make build_docs [path])"
	@echo "  build        : build the CLI binary (e.g. make build [path])"
	@echo "  test         : run tests locally"
	@echo "  run          : run the CLI (e.g. make run [args...])"
	@echo "  help         : show this help text"
	@echo "  all          : show this help text"

build_wasm:
	@echo "WASM Support is not implemented for cdd-java. See WASM.md"
