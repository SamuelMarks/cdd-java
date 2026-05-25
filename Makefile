.PHONY: install_base install_deps docs build_docs build test run help all default

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

docs:
	@echo "Building API docs to target/docs..."
	@rm -rf target/docs
	@mkdir -p target/docs
	@find src/main/java -name "*.java" ! -name "ApiIntegrationTest.java" > doc_sources.txt
	javadoc -d target/docs -cp "lib/*:src/main/java" @doc_sources.txt
	@rm -f doc_sources.txt
	@mkdir -p docs
	@rm -rf docs/html
	@ln -s ../target/docs docs/html

build_docs:
	@echo "Building API docs to $(DOCS_DIR)..."
	@mkdir -p "$(DOCS_DIR)"
	@find src/main/java -name "*.java" ! -name "ApiIntegrationTest.java" > doc_sources.txt
	javadoc -d "$(DOCS_DIR)" -cp "lib/*:src/main/java" @doc_sources.txt
	@rm -f doc_sources.txt

build:
	@echo "Building CLI to $(BIN_DIR)..."
	@mkdir -p "$(BIN_DIR)"
	@find src/main/java -name "*.java" ! -name "ApiIntegrationTest.java" > sources.txt
	javac -d "$(BIN_DIR)" -cp "lib/*:src/main/java" @sources.txt
	@rm -f sources.txt

test:
	@echo "Running tests..."
	mvn test

run:
	@if [ ! -f "$(BIN_DIR)/cli/Main.class" ]; then \
		$(MAKE) build BIN_DIR="$(BIN_DIR)"; \
	fi
	java -cp "lib/*:$(BIN_DIR)" cli.Main $(RUN_ARGS)

help:
	@echo "Available targets:"
	@echo "  install_base : install language runtime (Java JDK)"
	@echo "  install_deps : install local dependencies"
	@echo "  docs         : build the API docs to target/docs and symlink docs/html"
	@echo "  build_docs   : build the API docs (e.g. make build_docs [path])"
	@echo "  build        : build the CLI binary (e.g. make build [path])"
	@echo "  test         : run tests locally"
	@echo "  run          : run the CLI (e.g. make run [args...])"
	@echo "  build_wasm   : build WASM variant (Not implemented)"
	@echo "  build_docker : build Docker images"
	@echo "  run_docker   : run Docker images"
	@echo "  help         : show this help text"
	@echo "  all          : show this help text"

build_wasm:
	@echo "Building WASM variant..."
	bash build_wasm.sh
	mkdir -p bin
	cp target/wasm/cdd-java.wasm bin/ || true
	cp target/wasm/cdd-java.js* bin/ || true
DOCKER_CMD ?= $(shell if command -v docker >/dev/null 2>&1 && docker ps >/dev/null 2>&1; then echo docker; elif command -v nerdctl >/dev/null 2>&1 && nerdctl ps >/dev/null 2>&1; then echo nerdctl; elif command -v lima >/dev/null 2>&1 && lima nerdctl ps >/dev/null 2>&1; then echo "lima nerdctl"; else echo docker; fi)

build_docker:
	@echo "Building docker images using $(DOCKER_CMD)..."
	$(DOCKER_CMD) build -t cdd-java-alpine -f alpine.Dockerfile .
	$(DOCKER_CMD) build -t cdd-java-debian -f debian.Dockerfile .

run_docker:
	@echo "Testing docker images..."
	python3 test_docker.py
