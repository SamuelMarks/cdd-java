#!/bin/bash
set -e

if [ -z "$GRAALVM_HOME" ]; then
  echo "  [INFO] GRAALVM_HOME is not set. Skipping local GraalVM WASM compilation."
  exit 0
fi

if [ -z "$WASI_SDK_PATH" ]; then
  echo "  [INFO] WASI_SDK_PATH is not set. Skipping local GraalVM WASM compilation."
  exit 0
fi

echo "Starting GraalVM WASM compilation..."

mkdir -p target/wasm

$GRAALVM_HOME/bin/native-image \
  --target=wasm32-wasi \
  -H:+UnlockExperimentalVMOptions -H:WasiSdkPath=$WASI_SDK_PATH \
  --no-fallback \
  -cp target/cdd-java-0.0.1-jar-with-dependencies.jar \
  --initialize-at-build-time=com.github.javaparser \
  --initialize-at-build-time=org.json \
  -O3 \
   \
  cli.Main \
  -o target/wasm/cdd-java

if [ -f "target/wasm/cdd-java.js.wasm" ]; then
  mv target/wasm/cdd-java.js.wasm target/wasm/cdd-java.wasm
fi

