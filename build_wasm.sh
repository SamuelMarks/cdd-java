#!/bin/bash
set -e

if [ -z "$GRAALVM_HOME" ]; then
  echo "Error: GRAALVM_HOME is not set."
  exit 1
fi

if [ -z "$WASI_SDK_PATH" ]; then
  echo "Error: WASI_SDK_PATH is not set."
  exit 1
fi

echo "Starting GraalVM WASM compilation..."

mkdir -p target/wasm

$GRAALVM_HOME/bin/native-image \
  --target=wasm32-wasi \
  -H:WasiSdkPath=$WASI_SDK_PATH \
  --no-fallback \
  -cp target/cdd-java-0.0.1-jar-with-dependencies.jar \
  --initialize-at-build-time=com.github.javaparser \
  --initialize-at-build-time=org.json \
  -O3 \
  -g0 \
  cli.Main \
  -o target/wasm/cdd-java

