#!/bin/bash
set -e

if ! ls target/cdd-java-*-jar-with-dependencies.jar 1> /dev/null 2>&1; then
  echo "Jar not found. Running mvn package..."
  mvn clean package -DskipTests -Dexec.skip=true
fi

echo "Starting GraalVM WASI compilation..."
mkdir -p target/wasm

# Check if Docker is available to use the official GraalVM CE 22.3 image for reliable WASI compilation
if docker ps &> /dev/null || ( [ -S "$HOME/.rd/docker.sock" ] && DOCKER_HOST="unix://$HOME/.rd/docker.sock" "$HOME/.rd/bin/docker" ps &> /dev/null ) || ( [ -S "$HOME/.orbstack/run/docker.sock" ] && DOCKER_HOST="unix://$HOME/.orbstack/run/docker.sock" docker ps &> /dev/null ); then
  if docker ps &> /dev/null; then
    DOCKER_CMD="docker"
  elif [ -S "$HOME/.rd/docker.sock" ]; then
    export DOCKER_HOST="unix://$HOME/.rd/docker.sock"
    DOCKER_CMD="$HOME/.rd/bin/docker"
  elif [ -S "$HOME/.orbstack/run/docker.sock" ]; then
    export DOCKER_HOST="unix://$HOME/.orbstack/run/docker.sock"
    DOCKER_CMD="docker"
  else
    DOCKER_CMD="docker"
  fi
  echo "Using Docker (ubuntu) for WASI compilation to match release script exactly..."
  cat << 'EOF' > Dockerfile.wasi
FROM ubuntu:22.04
RUN apt-get update && apt-get install -y wget tar gzip ca-certificates curl build-essential maven
RUN cd /opt && \
    curl -sL https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-22.3.1/graalvm-ce-java17-linux-amd64-22.3.1.tar.gz | tar -xz && \
    ./graalvm-ce-java17-22.3.1/bin/gu install native-image && \
    curl -sL -o wasi-sdk.tar.gz https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-20/wasi-sdk-20.0-linux.tar.gz && \
    tar -xzf wasi-sdk.tar.gz && mv wasi-sdk-20.0+m wasi-sdk-20.0 || true
ENV GRAALVM_HOME=/opt/graalvm-ce-java17-22.3.1
ENV WASI_SDK_PATH=/opt/wasi-sdk-20.0
ENV JAVA_HOME=/opt/graalvm-ce-java17-22.3.1
ENV PATH=$JAVA_HOME/bin:$PATH
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests
RUN native-image --target=wasm32-wasi -H:WasiSdkPath=$WASI_SDK_PATH --no-fallback -cp target/cdd-java-*-jar-with-dependencies.jar --initialize-at-build-time=com.github.javaparser --initialize-at-build-time=org.json -O3 cli.Main -o target/wasm/cdd-java
EOF
  $DOCKER_CMD build --platform linux/amd64 -t cdd-java-wasi -f Dockerfile.wasi .
  $DOCKER_CMD run --platform linux/amd64 --rm -v "$(pwd)/target/wasm:/output" cdd-java-wasi cp /app/target/wasm/cdd-java /output/cdd-java.wasm
  rm Dockerfile.wasi
  
else
  # Fallback to local GraalVM if Docker is not available
  if [ -z "$GRAALVM_HOME" ]; then
    echo "Error: Docker is not installed and GRAALVM_HOME is not set."
    echo "A Linux GraalVM CE 22 installation is required to build WASI."
    exit 1
  fi
  if [ -z "$WASI_SDK_PATH" ]; then
    echo "Error: WASI_SDK_PATH is not set."
    exit 1
  fi
  "$GRAALVM_HOME/bin/native-image" \
    --target=wasm32-wasi \
    -H:WasiSdkPath="$WASI_SDK_PATH" \
    --no-fallback \
    -cp target/cdd-java-*-jar-with-dependencies.jar \
    --initialize-at-build-time=com.github.javaparser \
    --initialize-at-build-time=org.json \
    -O3 \
    cli.Main \
    -o target/wasm/cdd-java
fi

echo "Compilation successful!"
echo "Generated pure WASI standalone binary at target/wasm/cdd-java.wasm"
