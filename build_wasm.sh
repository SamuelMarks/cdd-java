#!/bin/bash
set -e

if [ -z "$GRAALVM_HOME" ]; then
  export GRAALVM_HOME="$(pwd)/graalvm-community-openjdk-22.0.1+8.1/Contents/Home"
fi

if [ -z "$WASI_SDK_PATH" ]; then
  export WASI_SDK_PATH="$(pwd)/wasi-sdk-24.0-arm64-macos"
fi

echo "Starting GraalVM WASM compilation..."

"$GRAALVM_HOME/bin/native-image" \
  --target=wasm32-wasi \
  -H:WasiSdkPath="$WASI_SDK_PATH" \
  --no-fallback \
  --initialize-at-build-time=com.github.javaparser \
  --initialize-at-build-time=org.json \
  -O3 -g0 \
  -cp target/cdd-java-0.0.1-jar-with-dependencies.jar \
  cli.Main \
  -o target/wasm/cdd-java

echo "Compilation successful!"
