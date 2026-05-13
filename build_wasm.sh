#!/bin/bash
set -e

if ! ls target/cdd-java-*-jar-with-dependencies.jar 1> /dev/null 2>&1; then
  echo "Jar not found. Running mvn package..."
  mvn clean package -DskipTests -Dexec.skip=true
fi

echo "Starting GraalVM WASI compilation..."
mkdir -p target/wasm

# Check if Docker is available to use the official GraalVM CE 22.3 image for reliable WASI compilation
if command -v docker &> /dev/null; then
  echo "Using Docker (ghcr.io/graalvm/native-image-community:22.3.1) for WASI compilation..."
  cat << 'EOF' > Dockerfile.wasi
FROM ghcr.io/graalvm/native-image-community:22.3.1
RUN microdnf install -y wget tar gzip
RUN wget -q https://github.com/WebAssembly/wasi-sdk/releases/download/wasi-sdk-20/wasi-sdk-20.0-linux.tar.gz && \
    tar -xzf wasi-sdk-20.0-linux.tar.gz && \
    mv wasi-sdk-20.0 /opt/wasi-sdk
WORKDIR /app
COPY . /app
RUN native-image --target=wasm32-wasi -H:WasiSdkPath=/opt/wasi-sdk --no-fallback -cp target/cdd-java-*-jar-with-dependencies.jar --initialize-at-build-time=com.github.javaparser --initialize-at-build-time=org.json -O3 cli.Main -o target/wasm/cdd-java
EOF
  docker build -t cdd-java-wasi -f Dockerfile.wasi .
  docker run --rm -v "$(pwd)/target/wasm:/output" cdd-java-wasi cp /app/target/wasm/cdd-java /output/cdd-java.wasm
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
